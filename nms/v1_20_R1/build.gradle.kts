plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.20.1-R0.1-SNAPSHOT", "../../tmp/1.20.1")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.20.1/authlib-4.0.43.jar"))
    "compileOnly"(files("../../tmp/1.20.1/datafixerupper-6.0.8.jar"))
}