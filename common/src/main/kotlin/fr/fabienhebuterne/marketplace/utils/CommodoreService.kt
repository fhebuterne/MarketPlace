package fr.fabienhebuterne.marketplace.utils

import com.mojang.brigadier.tree.LiteralCommandNode
import me.lucko.commodore.Commodore
import me.lucko.commodore.CommodoreProvider
import me.lucko.commodore.file.CommodoreFileReader
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin

class CommodoreService {

    fun init(plugin: JavaPlugin, command: PluginCommand?) {
        val commodore: Commodore = CommodoreProvider.getCommodore(plugin)
        val marketPlaceCommand: LiteralCommandNode<Any> =
            CommodoreFileReader.INSTANCE.parse(plugin.getResource("marketplace.commodore"))
        commodore.register(command, marketPlaceCommand)
    }

}