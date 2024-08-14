import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.readText

plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("com.github.breadmoirai.github-release") version "2.5.2"

    `maven-publish`
}

group = "dev.nyon"
val mcVersion = "1.20.4"
version = "2.0.2-$mcVersion"

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(
        loom.layered {
            parchment("org.parchmentmc.data:parchment-1.20.3:2023.12.31@zip")
            officialMojangMappings()
        }
    )
    implementation("org.vineflower:vineflower:1.9.3")
    implementation("net.fabricmc:fabric-loader:0.15.11")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    testImplementation(kotlin("test"))
}

tasks {
    register("release") {
        group = "publishing"

        dependsOn("githubRelease")
        dependsOn("publish")
    }

    withType<JavaCompile> {
        options.release.set(17)
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("dev.nyon.konfig.internal.InternalKonfigApi")
        }
    }
}

val changelogText =
    buildString {
        append("# v${project.version}\n")
        file("changelog.md").readText().also { append(it) }
    }

githubRelease {
    token(findProperty("github.token")?.toString())

    owner = "btwonion"
    repo = "konfig"
    releaseName = project.version.toString()
    tagName = project.version.toString()
    body = changelogText
    targetCommitish = "master"
    setReleaseAssets(tasks["remapJar"].outputs.files)
}

publishing {
    repositories {
        maven {
            name = "nyon"
            url = uri("https://repo.nyon.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.nyon"
            artifactId = "konfig"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
