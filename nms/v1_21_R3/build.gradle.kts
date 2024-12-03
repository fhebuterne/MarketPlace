plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.21.4-R0.1-SNAPSHOT", "../../tmp/1.21.4")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.21.4/authlib-6.0.57.jar"))
    "compileOnly"(files("../../tmp/1.21.4/datafixerupper-8.0.16.jar"))
}