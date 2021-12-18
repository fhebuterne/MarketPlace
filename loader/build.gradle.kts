import com.jetbrains.exposed.gradle.plugin.shadowjar.kotlinRelocate

plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":loader-utils"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.kotlinx}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}")
    compileOnly(project(":common"))
    compileOnly(files("../tmp/spigot-1.12.2.jar"))
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

val buildVersion: String? by project

tasks.shadowJar {
    if (buildVersion == null) {
        archiveFileName.set("MarketPlace-${archiveVersion.getOrElse("unknown")}.jar")
    } else {
        // For ci/cd
        archiveFileName.set("MarketPlace.jar")
    }

    relocate("kotlinx", "fr.fabienhebuterne.marketplace.libs.kotlinx")
    relocate("kotlin", "fr.fabienhebuterne.marketplace.libs.kotlin") {
        include("%regex[^kotlin/.*]")
    }

    relocate("org.intellij", "fr.fabienhebuterne.marketplace.libs.org.intellij")
    relocate("org.jetbrains.annotations", "fr.fabienhebuterne.marketplace.libs.org.jetbrains.annotations")

    exclude("DebugProbesKt.bin")
    exclude("module-info.class")

    destinationDirectory.set(file(System.getProperty("outputDir") ?: "$rootDir/build/"))

    from(project(":common").tasks.shadowJar.get().archiveFile)
}

tasks.build {
    dependsOn("shadowJar")
}
