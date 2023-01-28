plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version Versions.shadowJar
    kotlin("jvm") version Versions.kotlinJvm
    kotlin("plugin.serialization") version 1.8.0
    id("jacoco")
    id("com.jetbrains.exposed.gradle.plugin") version Versions.exposedGradlePlugin
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")

    jacoco {
        toolVersion = Versions.jacoco
    }

    group = "fr.fabienhebuterne"
    version = "1.7.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
    }

    defaultDependencies()

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinReflect}")
        testImplementation("io.mockk:mockk:${Versions.mockk}")
        testImplementation("io.strikt:strikt-core:${Versions.strikt}")
        testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(true)
        }
    }

    tasks.compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
