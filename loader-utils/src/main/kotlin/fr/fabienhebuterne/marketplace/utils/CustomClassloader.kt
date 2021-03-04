/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package fr.fabienhebuterne.marketplace.utils

import java.io.IOException
import java.lang.reflect.Constructor
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

// Thanks LuckPerms for jarinjar system to load external dependencies
class CustomClassloader(parent: ClassLoader, jarResourcePath: String) : URLClassLoader(
    arrayOf(
        extractJar(
            parent,
            jarResourcePath
        )
    ),
    parent
) {

    fun addJar(url: URL) {
        super.addURL(url)
    }

    fun <T> instantiatePlugin(bootstrapClass: String, loaderPluginType: Class<T>, loaderPlugin: T): BootstrapLoader {
        val plugin: Class<out BootstrapLoader>
        try {
            plugin = loadClass(bootstrapClass).asSubclass(BootstrapLoader::class.java)
        } catch (e: ReflectiveOperationException) {
            throw IllegalAccessException("Unable to load bootstrap class")
        }
        val constructor: Constructor<out BootstrapLoader> = try {
            plugin.getConstructor(loaderPluginType)
        } catch (e: ReflectiveOperationException) {
            throw IllegalAccessException("Unable to get bootstrap constructor")
        }
        return try {
            constructor.newInstance(loaderPlugin)
        } catch (e: ReflectiveOperationException) {
            e.printStackTrace()
            throw IllegalAccessException("Unable to create bootstrap plugin instance")
        }
    }

    companion object {
        private fun extractJar(loaderClassLoader: ClassLoader, jarResourcePath: String): URL {
            // get the jar-in-jar resource
            val jarInJar =
                loaderClassLoader.getResource(jarResourcePath)
                    ?: throw IllegalAccessException("Could not locate jar-in-jar")

            // create a temporary file
            // on posix systems by default this is only read/writable by the process owner
            val path: Path = try {
                Files.createTempFile("marketplace-jarinjar", ".jar.tmp")
            } catch (e: IOException) {
                throw IllegalAccessException("Unable to create a temporary file")
            }

            // mark that the file should be deleted on exit
            path.toFile().deleteOnExit()

            // copy the jar-in-jar to the temporary file path
            try {
                jarInJar.openStream().use { `in` -> Files.copy(`in`, path, StandardCopyOption.REPLACE_EXISTING) }
            } catch (e: IOException) {
                throw IllegalAccessException("Unable to copy jar-in-jar to temporary path")
            }
            return try {
                path.toUri().toURL()
            } catch (e: MalformedURLException) {
                throw IllegalAccessException("Unable to get URL from path")
            }
        }
    }
}
