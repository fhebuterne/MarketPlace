package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class MarketPlace : JavaPlugin() {
    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<MarketPlace>

    override fun onEnable() {
        callCommandFactoryInit = CallCommandFactoryInit(this, "marketplace")
    }

    override fun onDisable() {}

    override fun onCommand(sender: CommandSender,
                           command: Command,
                           commandLabel: String,
                           args: Array<String>): Boolean {
        return callCommandFactoryInit.onCommandCustomCraft(
                sender,
                command,
                commandLabel,
                args,
                MarketPlace::class.java.classLoader,
                "fr.fabienhebuterne.marketplace.commands",
                "marketplace.",
                true
        )
    }
}