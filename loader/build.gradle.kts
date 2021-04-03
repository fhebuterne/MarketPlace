plugins {
    id("java")
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":loader-utils"))
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
    minimize()

    if (buildVersion == null) {
        archiveFileName.set("MarketPlace-${archiveVersion.getOrElse("unknown")}.jar")
    } else {
        // For ci/cd
        archiveFileName.set("MarketPlace.jar")
    }

    destinationDirectory.set(file(System.getProperty("outputDir") ?: "$rootDir/build/"))

    from(project(":common").tasks.shadowJar.get().archiveFile)
}

tasks.build {
    dependsOn("shadowJar")
}
