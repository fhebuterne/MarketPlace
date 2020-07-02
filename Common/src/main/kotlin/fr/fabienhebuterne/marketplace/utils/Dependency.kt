package fr.fabienhebuterne.marketplace.utils

import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


class Dependency(var instance: JavaPlugin) {

    private var classLoader: URLClassLoader? = null

    fun downloadDependencies() {
        Paths.get(instance.dataFolder.path, "libs").toFile().mkdir()

        DependencyEnum.values()
                .filterNot { Paths.get(instance.dataFolder.path, "libs", it.nameDependency + "-" + it.version + ".jar").toFile().exists() }
                .forEach {
                    val inputStream: InputStream = URL(it.constructDownloadUrl()).openStream()

                    Bukkit.getLogger().info("Download ${it.nameDependency + "-" + it.version} dependency...")

                    Files.copy(inputStream, Paths.get(instance.dataFolder.path, "libs", it.nameDependency + "-" + it.version + ".jar"), StandardCopyOption.REPLACE_EXISTING)
                }
    }

    fun loadDependencies() {
        val classLoader: ClassLoader = instance.javaClass.classLoader
        if (classLoader is URLClassLoader) {
            this.classLoader = classLoader
        }

        val rules: MutableList<Relocation> = mutableListOf()
        rules.add(Relocation("com{}mysql".replace("{}", "."), "fr.fabienhebuterne.marketplace.libs.mysql"))
        rules.add(Relocation("org{}jetbrains{}kotlinx".replace("{}", "."), "fr.fabienhebuterne.marketplace.libs.kotlinx"))

        DependencyEnum.values()
                .filterNot { Paths.get(instance.dataFolder.path, "libs", it.nameDependency + "-" + it.version + "-relocated.jar").toFile().exists() }
                .forEach {
                    val relocator = JarRelocator(
                            File(instance.dataFolder.toString() + "/libs/${it.nameDependency}-${it.version}.jar"),
                            File(instance.dataFolder.toString() + "/libs/${it.nameDependency}-${it.version}-relocated.jar"),
                            rules
                    )

                    try {
                        Bukkit.getLogger().info("Relocated ${it.nameDependency + "-" + it.version} dependency...")
                        relocator.run()
                    } catch (e: IOException) {
                        throw RuntimeException("Unable to relocate", e)
                    }
                }


        try {
            val method: Method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
            method.isAccessible = true
            DependencyEnum.values().forEach {
                method.invoke(instance.javaClass.classLoader, Paths.get(instance.dataFolder.path, "libs", it.nameDependency + "-" + it.version + "-relocated.jar").toUri().toURL())
            }
        } catch (e: Exception) {
            throw RuntimeException("Unexpected exception", e)
        }
    }
}