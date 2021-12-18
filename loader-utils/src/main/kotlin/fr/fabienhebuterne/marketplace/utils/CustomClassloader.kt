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

import java.lang.reflect.Constructor
import java.net.URLClassLoader

// Thanks LuckPerms for jarinjar system to load external dependencies
class CustomClassloader(parent: ClassLoader) : URLClassLoader(
    arrayOf(),
    parent
) {

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

}
