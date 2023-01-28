plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.18.2-R0.1-SNAPSHOT", "../../tmp/1.18.2")

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    "compileOnly"(files("../../tmp/1.18.2/authlib-3.3.39.jar"))
    "compileOnly"(files("../../tmp/1.18.2/datafixerupper-4.1.27.jar"))
}