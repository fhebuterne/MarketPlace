plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.20.2-R0.1-SNAPSHOT", "../../tmp/1.20.2")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.20.2/authlib-5.0.47.jar"))
    "compileOnly"(files("../../tmp/1.20.2/datafixerupper-6.0.8.jar"))
}