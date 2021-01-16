import kotlin.system.exitProcess

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow")
    kotlin("plugin.serialization")
}

val buildVersion: String? by project

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
    implementation("joda-time:joda-time:2.10.6")

    // Tech Stack TEST dependency
    testCompileOnly("org.mockito:mockito-core:3.3.0")
    testCompileOnly("org.mockito:mockito-junit-jupiter:3.3.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter:5.6.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.6.0")

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

tasks.jar {
    archiveFileName.set("MarketPlace-${archiveVersion.getOrElse("unknown")}-minimal.jar")
}

tasks.shadowJar {
    minimize()

    if (buildVersion == null) {
        archiveFileName.set("MarketPlace-${archiveVersion.getOrElse("unknown")}.jar")
    } else {
        // For ci/cd
        archiveFileName.set("MarketPlace.jar")
    }

    destinationDirectory.set(file(System.getProperty("outputDir") ?: "$rootDir/build/"))

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
