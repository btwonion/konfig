import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.io.path.readText

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("fabric-loom") version "1.3-SNAPSHOT"
    id("com.github.breadmoirai.github-release") version "2.4.1"

    `maven-publish`
    signing
}

group = "dev.nyon"
val mcVersion = "1.20.2"
version = "1.0.3-$mcVersion"

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        parchment("org.parchmentmc.data:parchment-1.20.2:2023.10.22@zip")
        officialMojangMappings()
    })
    implementation("org.vineflower:vineflower:1.9.3")
    implementation("net.fabricmc:fabric-loader:0.14.24")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
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
        kotlinOptions.jvmTarget = "17"
    }
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("dev.nyon.konfig.internal.InternalKonfigApi")
        }
    }
}

val changelogFile: Path = rootDir.toPath().resolve("changelogs/${project.version}.md")
val changelogText = if (changelogFile.notExists()) "" else changelogFile.readText()

githubRelease {
    token(findProperty("github.token")?.toString())

    owner("btwonion")
    repo("konfig")
    releaseName(project.version.toString())
    tagName(project.version.toString())
    body(changelogText)
    targetCommitish("master")
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

signing {
    sign(publishing.publications)
}

java {
    withJavadocJar()
    withSourcesJar()
}