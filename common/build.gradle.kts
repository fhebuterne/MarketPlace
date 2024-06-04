import kotlin.system.exitProcess

plugins {
    id("java")
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.jetbrains.exposed.gradle.plugin")
    id("org.sonarqube")
}

sourceSets["main"].compileClasspath += files("${project.rootDir}/buildSrc/build/")

configurations["testImplementation"].extendsFrom(configurations["compileOnly"])

val buildVersion: String? by project

dependencies {
    // Tech Stack dependency
    implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed}")
    implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}")
    implementation("org.xerial:sqlite-jdbc:${Versions.sqlite}")
    implementation("com.mysql:mysql-connector-j:${Versions.mysqlDriver}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinx}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx}")
    compileOnly("com.github.MilkBowl:VaultAPI:${Versions.vault}")
    implementation("org.kodein.di:kodein-di-jvm:${Versions.kodein}")
    implementation("joda-time:joda-time:${Versions.jodaTime}")
    // Spigot doesn't have this dependency
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    implementation("me.lucko:commodore:${Versions.commodore}")
    compileOnly("com.mojang:brigadier:${Versions.brigadier}")

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

    compileOnly(project(":loader-utils"))
    implementation(project(":nms:Interfaces"))
    implementation(project(":nms:v1_12_R1"))
    implementation(project(":nms:v1_13_R2"))
    implementation(project(":nms:v1_14_R1"))
    implementation(project(":nms:v1_15_R1"))
    implementation(project(":nms:v1_16_R3"))
    implementation(project(":nms:v1_17_R1"))
    implementation(project(":nms:v1_18_R1"))
    implementation(project(":nms:v1_18_R2"))
    implementation(project(":nms:v1_19_R1"))
    implementation(project(":nms:v1_19_R2"))
    implementation(project(":nms:v1_19_R3"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
}