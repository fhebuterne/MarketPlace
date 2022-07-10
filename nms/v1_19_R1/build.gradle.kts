plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.19-R0.1-SNAPSHOT", "../../tmp/1.19")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.19/authlib-3.5.41.jar"))
    "compileOnly"(files("../../tmp/1.19/datafixerupper-5.0.28.jar"))
}