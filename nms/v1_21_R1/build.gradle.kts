plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.21-R0.1-SNAPSHOT", "../../tmp/1.21")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.21/authlib-6.0.54.jar"))
    "compileOnly"(files("../../tmp/1.21/datafixerupper-8.0.16.jar"))
}