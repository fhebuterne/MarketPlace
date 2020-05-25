package fr.fabienhebuterne.marketplace.utils

import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

class Dependency(var instance: JavaPlugin) {

    private var classLoader: URLClassLoader? = null

    private val dependencies = mutableListOf("slf4j-api-1.7.30", "kotlin-stdlib-1.3.71", "kotlin-stdlib-jdk8-1.3.71",
            "kotlin-reflect-1.3.71", "mysql-connector-java-8.0.19", "exposed-jdbc-0.23.1", "exposed-dao-0.23.1", "exposed-core-0.23.1",
            "kotlinx-serialization-runtime-0.20.0", "kodein-di-generic-jvm-6.5.4", "kodein-di-core-jvm-6.5.4")

    fun loadDependencies() {
        val classLoader: ClassLoader = instance.javaClass.classLoader
        if (classLoader is URLClassLoader) {
            this.classLoader = classLoader
        }

        val rules: MutableList<Relocation> = mutableListOf()
        rules.add(Relocation("com{}mysql".replace("{}", "."), "fr.fabienhebuterne.marketplace.libs.mysql"))
        rules.add(Relocation("org{}jetbrains{}kotlinx".replace("{}", "."), "fr.fabienhebuterne.marketplace.libs.kotlinx"))

        val files = dependencies.map { dependency ->
            val relocator = JarRelocator(
                    File(instance.dataFolder.toString() + "/libs/$dependency.jar"),
                    File(instance.dataFolder.toString() + "/libs/$dependency-relocated.jar"),
                    rules
            )

            try {
                println("relocated run for : $dependency")
                relocator.run()
            } catch (e: IOException) {
                throw RuntimeException("Unable to relocate", e)
            }

            File(instance.dataFolder.toString() + "/libs/$dependency-relocated.jar")
        }

        try {
            val method: Method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
            method.isAccessible = true
            files.forEach {
                method.invoke(this.classLoader, it.toURI().toURL())
            }
        } catch (e: Exception) {
            throw RuntimeException("Unexpected exception", e)
        }
    }
}