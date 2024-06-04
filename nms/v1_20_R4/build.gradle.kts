plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.20.6-R0.1-SNAPSHOT", "../../tmp/1.20.6")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.20.6/authlib-6.0.54.jar"))
    "compileOnly"(files("../../tmp/1.20.6/datafixerupper-7.0.14.jar"))
}