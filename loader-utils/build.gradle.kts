plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    compileOnly(files("../tmp/spigot-1.12.2.jar"))
}
