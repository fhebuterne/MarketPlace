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
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class Dependency(var instance: JavaPlugin) {

    private var classLoader: URLClassLoader? = null
    private val urlMaven = "https://mvnrepository.com/artifact/{{group}}/{{name}}/{{version}}"

    fun downloadDependencies() {
        Paths.get(instance.dataFolder.path, "libs").toFile().mkdir()

        DependencyEnum.values()
                .filterNot { Paths.get(instance.dataFolder.path, "libs", it.nameDependency + "-" + it.version + ".jar").toFile().exists() }
                .forEach {
                    val urlMavenDownload = urlMaven.replace("{{group}}", it.group.replace("{}", "."))
                            .replace("{{name}}", it.nameDependency)
                            .replace("{{version}}", it.version)

                    val urlConnection: URLConnection
                    val url = URL(urlMavenDownload)
                    urlConnection = url.openConnection()
                    urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36")
                    val sc = Scanner(urlConnection.getInputStream())
                    val sb = StringBuffer()
                    while (sc.hasNext()) {
                        sb.append(sc.next())
                    }
                    val result = sb.toString()

                    val pattern: Pattern = Pattern.compile("https://(repo1\\.maven\\.org|dl\\.bintray\\.com).*/${it.nameDependency}-${it.version}\\.jar", Pattern.CASE_INSENSITIVE)
                    val urlMatcher: Matcher = pattern.matcher(result)
                    urlMatcher.find()
                    val jarDownloadUrl = urlMatcher.group(0)
                    val inputStream: InputStream = URL(jarDownloadUrl).openStream()

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
                .map {
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

                    File(instance.dataFolder.toString() + "/libs/${it.nameDependency}-${it.version}-relocated.jar")
                }


        try {
            val method: Method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
            method.isAccessible = true
            DependencyEnum.values().forEach {
                method.invoke(this.classLoader, Paths.get(instance.dataFolder.path, "libs", it.nameDependency + "-" + it.version + "-relocated.jar").toUri().toURL())
            }
        } catch (e: Exception) {
            throw RuntimeException("Unexpected exception", e)
        }
    }
}