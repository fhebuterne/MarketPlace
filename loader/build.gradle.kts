plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("org.sonarqube")
}

dependencies {
    implementation(project(":loader-utils"))
    compileOnly(project(":common"))
    compileOnly(files("../tmp/spigot-1.12.2.jar"))
}

tasks.processResources {
    filesMatching("**/**.yml") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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

    exclude("DebugProbesKt.bin")
    exclude("module-info.class")

    destinationDirectory.set(file(System.getProperty("outputDir") ?: "$rootDir/build/"))

    from(project(":common").tasks.shadowJar.get().archiveFile)
}

tasks.build {
    dependsOn("shadowJar")
}