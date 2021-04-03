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

package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.utils.BootstrapLoader
import fr.fabienhebuterne.marketplace.utils.CustomClassloader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

// Thanks LuckPerms for jarinjar system to load external dependencies
class MarketPlaceLoader : JavaPlugin() {

    private val jarName = "marketplace.jarinjar"
    private val bootstrapClass = "fr.fabienhebuterne.marketplace.MarketPlace"
    var instance: BootstrapLoader
    var loader: CustomClassloader = CustomClassloader(javaClass.classLoader, jarName)

    init {
        this.instance = loader.instantiatePlugin(bootstrapClass, JavaPlugin::class.java, this)
    }

    override fun onLoad() {
        GlobalScope.launch {
            coroutineScope {
                instance.onLoad()
            }
        }
    }

    override fun onEnable() {
        GlobalScope.launch {
            coroutineScope {
                instance.onEnable()
            }
        }
    }

    override fun onDisable() {
        instance.onDisable()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        return instance.onCommand(sender, command, label, args)
    }
}
