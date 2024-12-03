plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.21.3-R0.1-SNAPSHOT", "../../tmp/1.21.3")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.3-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.21.3/authlib-6.0.55.jar"))
    "compileOnly"(files("../../tmp/1.21.3/datafixerupper-8.0.16.jar"))
}