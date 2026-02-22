plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.21.10-R0.1-SNAPSHOT", "../../tmp/1.21.10")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.10-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.21.10/authlib-7.0.61.jar"))
    "compileOnly"(files("../../tmp/1.21.10/datafixerupper-8.0.16.jar"))
}