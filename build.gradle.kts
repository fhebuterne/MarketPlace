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
    maven ("https://hub.spigotmc.org/nexus/content/repositories/snapshots/" )
    maven ( "https://oss.sonatype.org/content/repositories/snapshots" )
    jcenter()
}

dependencies {
    implementation("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation("org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT")
    implementation("org.kodein.di", "kodein-di-generic-jvm", "6.5.3")
    testCompileOnly("org.mockito:mockito-core:3.3.0")
    testCompileOnly("org.mockito:mockito-junit-jupiter:3.3.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter:5.6.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.6.0")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.jetbrains.exposed", "exposed-core", "0.22.1")
    compile("org.jetbrains.exposed", "exposed-dao", "0.22.1")
    compile("org.jetbrains.exposed", "exposed-jdbc", "0.22.1")
    compile("mysql", "mysql-connector-java", "8.0.19")
    compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
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
    configurations = mutableListOf(project.configurations.compile.get())
    mergeServiceFiles()
    minimize()
}