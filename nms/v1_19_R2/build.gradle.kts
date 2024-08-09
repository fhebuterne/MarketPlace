plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.19.3-R0.1-SNAPSHOT", "../../tmp/1.19.3")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.19.3/authlib-3.16.29.jar"))
    "compileOnly"(files("../../tmp/1.19.3/datafixerupper-5.0.28.jar"))
}