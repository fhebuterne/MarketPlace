package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.config.Config
import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.listeners.AsyncPlayerChatEventListener
import fr.fabienhebuterne.marketplace.listeners.InventoryClickEventListener
import fr.fabienhebuterne.marketplace.listeners.PlayerJoinEventListener
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.LogsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import fr.fabienhebuterne.marketplace.storage.mysql.*
import fr.fabienhebuterne.marketplace.utils.Dependency
import kotlinx.serialization.ImplicitReflectionSerializer
import net.milkbowl.vault.economy.Economy
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton


class MarketPlace : JavaPlugin() {
    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<MarketPlace>
    private var econ: Economy? = null
    lateinit var config: ConfigService<Config>
    lateinit var kodein: Kodein
    lateinit var instance: MarketPlace

    @ImplicitReflectionSerializer
    override fun onEnable() {
        instance = this
        Dependency(this, this.classLoader).loadDependencies()

        if (!setupEconomy()) {
            this.logger.severe("Disabled due to no Economy plugin found!")
            server.pluginManager.disablePlugin(this)
            return
        }

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
            SchemaUtils.create(ListingsTable, MailsTable, LogsTable)
        }

        kodein = Kodein {
            bind<ListingsRepository>() with singleton { ListingsRepositoryImpl(database) }
            bind<MailsRepository>() with singleton { MailsRepositoryImpl(database) }
            bind<LogsRepository>() with singleton { LogsRepositoryImpl(database) }
            bind<ListingsService>() with singleton { ListingsService(instance(), instance()) }
            bind<MailsService>() with singleton { MailsService(instance()) }
            bind<LogsService>() with singleton { LogsService(instance()) }
            bind<ListingsInventoryService>() with singleton { ListingsInventoryService(instance(), instance()) }
            bind<MailsInventoryService>() with singleton { MailsInventoryService(instance()) }
            bind<MarketService>() with singleton { MarketService(instance, instance(), instance(), instance(), instance(), instance()) }
        }

        // TODO : Create factory to init listeners
        server.pluginManager.registerEvents(InventoryClickEventListener(this, kodein), this)
        server.pluginManager.registerEvents(AsyncPlayerChatEventListener(this, kodein), this)
        server.pluginManager.registerEvents(PlayerJoinEventListener(this, ListingsRepositoryImpl(database)), this)
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

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(Economy::class.java)
                ?: return false
        econ = rsp.provider;
        return econ != null;
    }

    fun getEconomy(): Economy {
        return econ ?: throw Exception("Cannot found economy plugin...")
    }
}