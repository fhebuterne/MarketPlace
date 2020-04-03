package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.Config
import fr.fabienhebuterne.marketplace.domain.ConfigService
import kotlinx.serialization.ImplicitReflectionSerializer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class MarketPlace : JavaPlugin() {
    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<MarketPlace>
    lateinit var config: ConfigService<Config>

    @ImplicitReflectionSerializer
    override fun onEnable() {
        config = ConfigService(this, "config", Config::class)
        config.createOrLoadConfig(false)

        callCommandFactoryInit = CallCommandFactoryInit(this, "marketplace")

        /*val database = Database.connect(
                url = "jdbc:mysql://${config.database.hostname}:${config.database.port}/${config.database.database}?useSSL=false",
                driver = "com.mysql.jdbc.Driver",
                user = config.database.username,
                password = config.database.password
        )*/

    }

    override fun onDisable() {}

    override fun onCommand(sender: CommandSender,
                           command: Command,
                           commandLabel: String,
                           args: Array<String>): Boolean {
        return callCommandFactoryInit.onCommand(
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