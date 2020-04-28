package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.ItemsTable
import fr.fabienhebuterne.marketplace.domain.ListingsTable
import fr.fabienhebuterne.marketplace.domain.config.Config
import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.listeners.InventoryClickEventListener
import fr.fabienhebuterne.marketplace.storage.ItemsRepository
import fr.fabienhebuterne.marketplace.storage.ItemsRepositoryImpl
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.ListingsRepositoryImpl
import fr.fabienhebuterne.marketplace.utils.Dependency
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
    lateinit var kodein: Kodein

    @ImplicitReflectionSerializer
    override fun onEnable() {
        Dependency(this, this.classLoader).loadDependencies()

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

        kodein = Kodein {
            bind<ItemsRepository>() with singleton { ItemsRepositoryImpl(database) }
            bind<ListingsRepository>() with singleton { ListingsRepositoryImpl(database) }
        }

        // TODO : Delete after all tests
        /*val itemsRepositoryImpl = ItemsRepositoryImpl(database)
        val create = itemsRepositoryImpl.create(Items(
                id = UUID.randomUUID(),
                item = ItemStack(Material.APPLE)
        ))
        val listingsRepositoryImpl = ListingsRepositoryImpl(database)
        listingsRepositoryImpl.create(Listings(
                "test",
                "fab",
                create.id,
                1,
                1,
                100,
                "world",
                System.currentTimeMillis()
        ))*/

        server.pluginManager.registerEvents(InventoryClickEventListener(this, ListingsRepositoryImpl(database)), this)
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
                true,
                kodein
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