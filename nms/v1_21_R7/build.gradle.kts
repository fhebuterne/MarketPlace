plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.21.11-R0.2-SNAPSHOT", "../../tmp/1.21.11")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.11-R0.2-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.21.11/authlib-7.0.61.jar"))
    "compileOnly"(files("../../tmp/1.21.11/datafixerupper-9.0.19.jar"))
}