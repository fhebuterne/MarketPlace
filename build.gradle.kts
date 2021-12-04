plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version Versions.shadowJar
    kotlin("jvm") version Versions.kotlinJvm
    kotlin("plugin.serialization") version Versions.kotlinSerialization
    id("jacoco")
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")

    jacoco {
        toolVersion = "0.8.6"
    }

    group = "fr.fabienhebuterne"
    version = "1.3.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
    }

    defaultDependencies()

    dependencies {
        implementation("${Artefacts.kotlinGroup}:kotlin-reflect:${Versions.kotlinReflect}")
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
            xml.isEnabled = true
            csv.isEnabled = true
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
