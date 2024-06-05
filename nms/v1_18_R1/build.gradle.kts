plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.18-R0.1-SNAPSHOT", "../../tmp/1.18")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.18/authlib-3.2.38.jar"))
    "compileOnly"(files("../../tmp/1.18/datafixerupper-4.0.26.jar"))
}