import kotlin.system.exitProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

configurations["testImplementation"].extendsFrom(configurations["compileOnly"])

val buildVersion: String? by project

dependencies {
    testImplementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("stdlib-jdk8"))

    // Tech Stack dependency
    implementation("org.kodein.di", "kodein-di-jvm", "7.3.1")
    compileOnly("org.jetbrains.exposed", "exposed-core", "0.23.1")
    compileOnly("org.jetbrains.exposed", "exposed-dao", "0.23.1")
    compileOnly("org.jetbrains.exposed", "exposed-jdbc", "0.23.1")
    compileOnly("mysql", "mysql-connector-java", "8.0.19")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc")
    implementation("me.lucko", "jar-relocator", "1.3")
    implementation("joda-time:joda-time:2.10.6")

    // Plugin dependency
    if (buildVersion == null) {
        println("build common module with spigot 1.12.2")
        compileOnly(files("../tmp/spigot-1.12.2.jar"))
    } else {
        println("build common module with spigot $buildVersion")

        if (File("$rootDir/tmp/spigot-$buildVersion.jar").exists()) {
            compileOnly(files("../tmp/spigot-$buildVersion.jar"))
        } else {
            println("file spigot-$buildVersion doesn't exist cancel build")
            exitProcess(1)
        }
    }

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly(project(":loader-utils"))

    implementation(project(":nms:Interfaces"))
    implementation(project(":nms:v1_12_R1"))
    implementation(project(":nms:v1_13_R2"))
    implementation(project(":nms:v1_14_R1"))
    implementation(project(":nms:v1_15_R1"))
    implementation(project(":nms:v1_16_R3"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    minimize()

    archiveFileName.set("marketplace.jarinjar")

    dependencies {
        exclude(dependency("org.jetbrains.exposed:*"))
        exclude(dependency("org.jetbrains.kotlinx:*"))
        exclude(dependency("com.mysql:*"))
    }

    relocate("com.mysql", "fr.fabienhebuterne.marketplace.libs.mysql")
    relocate("org.jetbrains.kotlinx", "fr.fabienhebuterne.marketplace.libs.kotlinx")
}

tasks.build {
    dependsOn("shadowJar")
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
