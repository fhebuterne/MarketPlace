plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.71"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    kotlin("plugin.serialization") version "1.3.71"
}

group = "fr.fabienhebuterne"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    jcenter()
}

dependencies {
    // Tech Stack dependency
    compileOnly("org.kodein.di", "kodein-di-generic-jvm", "6.5.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.71")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.71")
    compileOnly("org.jetbrains.exposed", "exposed-core", "0.23.1")
    compileOnly("org.jetbrains.exposed", "exposed-dao", "0.23.1")
    compileOnly("org.jetbrains.exposed", "exposed-jdbc", "0.23.1")
    compileOnly("mysql", "mysql-connector-java", "8.0.19")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation("me.lucko", "jar-relocator", "1.3")

    // Tech Stack TEST dependency
    testCompileOnly("org.mockito:mockito-core:3.3.0")
    testCompileOnly("org.mockito:mockito-junit-jupiter:3.3.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter:5.6.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.6.0")

    // Plugin dependency
    compileOnly(files("tmp/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

tasks.test {
    useJUnitPlatform()
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

tasks.processResources {
    filesMatching("**/**.yml") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        expand(project.properties)
    }
}

tasks.shadowJar {
    minimize()

    destinationDirectory.set(file(System.getProperty("outputDir") ?: "$rootDir/build/"))

    dependencies {
        exclude(dependency("org.jetbrains.exposed:*"))
        exclude(dependency("org.jetbrains.kotlinx:*"))
        exclude(dependency("com.mysql:*"))
    }

    relocate("com.mysql", "fr.fabienhebuterne.marketplace.libs.mysql")
    relocate("org.jetbrains.kotlinx", "fr.fabienhebuterne.marketplace.libs.kotlinx")
}