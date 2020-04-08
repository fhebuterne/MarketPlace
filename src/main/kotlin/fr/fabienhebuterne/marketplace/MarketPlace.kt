package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.ItemsTable
import fr.fabienhebuterne.marketplace.domain.ListingsTable
import fr.fabienhebuterne.marketplace.domain.config.Config
import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.storage.ItemsRepository
import fr.fabienhebuterne.marketplace.storage.ItemsRepositoryImpl
import kotlinx.serialization.ImplicitReflectionSerializer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.sql.ResultSet

class MarketPlace : JavaPlugin() {
    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<MarketPlace>
    lateinit var config: ConfigService<Config>

    @ImplicitReflectionSerializer
    override fun onEnable() {
        config = ConfigService(this, "config", Config::class)
        config.createOrLoadConfig(false)
        val configParsed = config.getSerialization()

        callCommandFactoryInit = CallCommandFactoryInit(this, "marketplace")

        val database = Database.connect(
                url = "jdbc:mysql://${configParsed.database.hostname}:${configParsed.database.port}/${configParsed.database.database}?useSSL=false&characterEncoding=UTF-8",
                driver = "com.mysql.cj.jdbc.Driver",
                user = configParsed.database.username,
                password = configParsed.database.password
        )

        transaction {
            SchemaUtils.create(ItemsTable, ListingsTable)
            //"ALTER TABLE marketplace_items ENGINE = InnoDB;".execAndMap {}
            //"ALTER TABLE marketplace_listings ENGINE = InnoDB;".execAndMap {}
        }

        Kodein {
            bind<ItemsRepository>() with singleton { ItemsRepositoryImpl(database) }
        }

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

    // TODO : Move in utils class
    fun <T : Any> String.execAndMap(transform: (ResultSet) -> T): List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(this) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }

}