plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
}

allprojects {
    apply(plugin = "kotlin")

    group = "fr.fabienhebuterne"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        jcenter()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.31")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        testCompileOnly("org.mockito:mockito-core:3.3.0")
        testCompileOnly("org.mockito:mockito-junit-jupiter:3.3.0")
        testCompileOnly("org.junit.jupiter:junit-jupiter:5.6.0")
        testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.6.0")
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
