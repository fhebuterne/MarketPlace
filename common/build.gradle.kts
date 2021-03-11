import kotlin.system.exitProcess

plugins {
    id("java")
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

configurations["testImplementation"].extendsFrom(configurations["compileOnly"])

val buildVersion: String? by project

dependencies {
    // Tech Stack dependency
    compileOnly("org.jetbrains.exposed:exposed-core:${Versions.exposed}")
    compileOnly("org.jetbrains.exposed:exposed-dao:${Versions.exposed}")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}")
    compileOnly("mysql:mysql-connector-java:${Versions.mysqlDriver}")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.kotlinx}")
    compileOnly("com.github.MilkBowl:VaultAPI:${Versions.vault}")
    implementation("org.kodein.di:kodein-di-jvm:${Versions.kodein}")
    implementation("me.lucko:jar-relocator:${Versions.jarRelocator}")
    implementation("joda-time:joda-time:${Versions.jodaTime}")

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
