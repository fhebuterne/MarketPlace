import kotlin.system.exitProcess

plugins {
    id("java")
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val buildVersion: String? by project

dependencies {
    implementation(project(":loader-utils"))
    compileOnly(project(":common"))

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
}

tasks.test {
    useJUnitPlatform()
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

    from(project(":common").tasks.shadowJar.get().archiveFile)
}

tasks.build {
    dependsOn("shadowJar")
}
