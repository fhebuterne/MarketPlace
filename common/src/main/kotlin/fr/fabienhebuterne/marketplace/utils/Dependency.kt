package fr.fabienhebuterne.marketplace.utils

import fr.fabienhebuterne.marketplace.MarketPlace
import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import org.bukkit.Bukkit
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


class Dependency(val instance: MarketPlace) {
    fun downloadDependencies() {
        Paths.get(instance.loader.dataFolder.path, "libs").toFile().mkdirs()

        DependencyEnum.values()
            .filterNot {
                Paths.get(instance.loader.dataFolder.path, "libs", it.nameDependency + "-" + it.version + ".jar")
                    .toFile()
                    .exists()
            }
            .forEach {
                val inputStream: InputStream = URL(it.constructDownloadUrl()).openStream()

                Bukkit.getLogger().info("Download ${it.nameDependency + "-" + it.version} dependency...")

                Files.copy(
                    inputStream,
                    Paths.get(instance.loader.dataFolder.path, "libs", it.nameDependency + "-" + it.version + ".jar"),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
    }

    fun loadDependencies() {
        val rules: MutableList<Relocation> = mutableListOf()
        rules.add(Relocation("com{}mysql".replace("{}", "."), "fr.fabienhebuterne.marketplace.libs.mysql"))
        rules.add(
            Relocation(
                "org{}jetbrains{}kotlinx".replace("{}", "."),
                "fr.fabienhebuterne.marketplace.libs.kotlinx"
            )
        )

        DependencyEnum.values()
            .filterNot {
                Paths.get(
                    instance.loader.dataFolder.path,
                    "libs",
                    it.nameDependency + "-" + it.version + "-relocated.jar"
                ).toFile().exists()
            }
            .forEach {
                val relocator = JarRelocator(
                    File(instance.loader.dataFolder.toString() + "/libs/${it.nameDependency}-${it.version}.jar"),
                    File(instance.loader.dataFolder.toString() + "/libs/${it.nameDependency}-${it.version}-relocated.jar"),
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
            DependencyEnum.values().forEach {
                instance.customClassloaderAppender.addJarToClasspath(
                    Paths.get(
                        instance.loader.dataFolder.path,
                        "libs",
                        it.nameDependency + "-" + it.version + "-relocated.jar"
                    )
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("Unexpected exception", e)
        }
    }
}
