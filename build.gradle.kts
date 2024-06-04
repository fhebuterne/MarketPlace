plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version Versions.shadowJar
    kotlin("jvm") version Versions.kotlinJvm
    kotlin("plugin.serialization") version Versions.kotlinSerialization
    id("jacoco")
    id("com.jetbrains.exposed.gradle.plugin") version Versions.exposedGradlePlugin
    id("org.sonarqube") version Versions.sonarQube
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.johnrengelman.shadow")

    jacoco {
        toolVersion = Versions.jacoco
    }

    group = "fr.fabienhebuterne"
    version = System.getProperty("tagVersion") ?: "SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        maven("https://libraries.minecraft.net")
    }

    defaultDependencies()

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinReflect}")
        testImplementation("io.mockk:mockk:${Versions.mockk}")
        testImplementation("io.strikt:strikt-core:${Versions.strikt}")
        testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    }

    configurations.all {
        resolutionStrategy {
            // to avoid duplication on shadowJar
            force("org.jetbrains:annotations:13.0")
            force("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinJvm}")
            force("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinReflect}")
        }
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

    tasks.compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.compileTestJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    // TODO : How to relocate kotlin, annotations ? issue caused by duplicated versions
    tasks.shadowJar {
        mergeServiceFiles()

        // relocate kotlin libs
        relocate("kotlinx", "fr.fabienhebuterne.marketplace.libs.kotlinx")
        relocate("org.kodein", "fr.fabienhebuterne.marketplace.libs.org.kodein")
        // relocate java libs
        //relocate("org.intellij", "fr.fabienhebuterne.marketplace.libs.org.intellij")
        //relocate("org.jetbrains.annotations", "fr.fabienhebuterne.marketplace.libs.org.jetbrains.annotations")
        relocate("org.joda.time", "fr.fabienhebuterne.marketplace.libs.org.joda.time")
        relocate("com.mysql", "fr.fabienhebuterne.marketplace.libs.mysql")
        relocate("org.slf4j", "fr.fabienhebuterne.marketplace.libs.org.slf4j")
        relocate("com.google", "fr.fabienhebuterne.marketplace.libs.com.google")
        relocate("org.yaml", "fr.fabienhebuterne.marketplace.libs.org.yaml")
        // relocate mc libs
        relocate("me.lucko.commodore", "fr.fabienhebuterne.marketplace.libs.commodore")

        exclude("DebugProbesKt.bin")
        exclude("module-info.class")

        dependencies {
            exclude(dependency("com.mojang:brigadier"))
        }
    }

    tasks.build {
        dependsOn("shadowJar")
    }
}