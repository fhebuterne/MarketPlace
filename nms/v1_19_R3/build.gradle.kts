plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.19.4-R0.1-SNAPSHOT", "../../tmp/1.19.4")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.19.4/authlib-3.17.30.jar"))
    "compileOnly"(files("../../tmp/1.19.4/datafixerupper-6.0.6.jar"))
}