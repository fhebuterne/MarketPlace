plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.20.4-R0.1-SNAPSHOT", "../../tmp/1.20.4")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.20.4/authlib-5.0.51.jar"))
    "compileOnly"(files("../../tmp/1.20.4/datafixerupper-6.0.8.jar"))
}