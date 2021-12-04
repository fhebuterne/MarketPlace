plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

nmsDependencies("1.18-R0.1-SNAPSHOT")

dependencies {
    "compileOnly"(files("../../tmp/spigot-api-1.18.jar"))
    "compileOnly"(files("../../tmp/authlib-3.2.38.jar"))
    "compileOnly"(files("../../tmp/datafixerupper-4.0.26.jar"))
}