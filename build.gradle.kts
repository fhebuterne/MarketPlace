plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.71"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    kotlin("plugin.serialization") version "1.3.71"
}

allprojects {
    group = "fr.fabienhebuterne"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        jcenter()
    }
}