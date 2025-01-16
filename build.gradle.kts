import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.breadmoirai.github-release") version "2.5.2"

    `maven-publish`
}

group = "dev.nyon"
version = "2.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

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
    token(providers.environmentVariable("GITHUB_TOKEN"))

    owner = "btwonion"
    repo = "konfig"
    releaseName = project.version.toString()
    tagName = project.version.toString()
    body = changelogText
    targetCommitish = "master"
    setReleaseAssets(tasks["jar"].outputs.files)
}

publishing {
    repositories {
        maven {
            name = "nyon"
            url = uri("https://repo.nyon.dev/releases")
            credentials {
                username = providers.environmentVariable("NYON_USERNAME").orNull
                password = providers.environmentVariable("NYON_PASSWORD").orNull
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
