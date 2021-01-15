package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.config.Config
import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.domain.config.Translation
import fr.fabienhebuterne.marketplace.domain.loadSkull
import fr.fabienhebuterne.marketplace.listeners.AsyncPlayerChatEventListener
import fr.fabienhebuterne.marketplace.listeners.InventoryClickEventListener
import fr.fabienhebuterne.marketplace.listeners.PlayerJoinEventListener
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import fr.fabienhebuterne.marketplace.services.ExpirationService
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
import org.bukkit.Bukkit
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

lateinit var tl: Translation
lateinit var conf: Config

class MarketPlace : JavaPlugin() {
    companion object {
        var isReload: Boolean = false
        lateinit var itemStackReflection: IItemStackReflection
    }

    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<MarketPlace>
    private var econ: Economy? = null
    lateinit var translation: ConfigService<Translation>
    lateinit var configService: ConfigService<Config>
    lateinit var kodein: Kodein
    lateinit var instance: MarketPlace

    @ImplicitReflectionSerializer
    override fun onEnable() {
        instance = this
        val dependency = Dependency(this)
        dependency.downloadDependencies()
        dependency.loadDependencies()

        if (!setupEconomy()) {
            this.logger.severe("Disabled due to no Economy plugin found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        // TODO : Add method to check missing key/value in current file (compare with resource jar file)

        configService = ConfigService(this, "config", Config::class)
        configService.createAndLoadConfig(false)
        conf = configService.getSerialization()

        translation = ConfigService(this, "translation-fr", Translation::class)
        translation.createAndLoadConfig(true)
        tl = translation.getSerialization()

        callCommandFactoryInit = CallCommandFactoryInit(this, "marketplace")

        val database = Database.connect(
                url = "jdbc:mysql://${conf.database.hostname}:${conf.database.port}/${conf.database.database}?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC",
                driver = "fr.fabienhebuterne.marketplace.libs.mysql.cj.jdbc.Driver",
                user = conf.database.username,
                password = conf.database.password
        )

        transaction {
            SchemaUtils.create(ListingsTable, MailsTable, LogsTable)
        }

        kodein = Kodein {
            bind<IItemStackReflection>() with singleton { initItemStackNms() ?: throw Exception() }
            bind<ListingsRepository>() with singleton { ListingsRepositoryImpl(database) }
            bind<MailsRepository>() with singleton { MailsRepositoryImpl(database) }
            bind<LogsRepository>() with singleton { LogsRepositoryImpl(database) }
            bind<ListingsService>() with singleton { ListingsService(instance, instance(), instance()) }
            bind<MailsService>() with singleton { MailsService(instance, instance()) }
            bind<LogsService>() with singleton { LogsService(instance()) }
            bind<ListingsInventoryService>() with singleton { ListingsInventoryService(instance()) }
            bind<MailsInventoryService>() with singleton { MailsInventoryService(instance()) }
            bind<MarketService>() with singleton { MarketService(instance, instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
            bind<ExpirationService>() with singleton { ExpirationService(instance, instance(), instance(), instance()) }
        }

        val itemStackReflection: IItemStackReflection by kodein.instance()
        MarketPlace.itemStackReflection = itemStackReflection
        loadSkull(itemStackReflection)

        // TODO : Create factory to init listeners
        server.pluginManager.registerEvents(InventoryClickEventListener(this, kodein), this)
        server.pluginManager.registerEvents(AsyncPlayerChatEventListener(this, kodein), this)
        server.pluginManager.registerEvents(PlayerJoinEventListener(ListingsRepositoryImpl(database), MailsRepositoryImpl(database)), this)

        // Start tasks to check items expired
        val expirationService: ExpirationService by kodein.instance()
        expirationService.startTaskExpirationListingsToMails()
        expirationService.startTaskExpirationMailsToDelete()
    }

    private fun initItemStackNms(): IItemStackReflection? {
        val clazzVersion = Bukkit.getServer().javaClass.getPackage().name

        if (clazzVersion.contains("v1_12_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_12_R1.ItemStackReflection
        }

        if (clazzVersion.contains("v1_13_R2")) {
            return fr.fabienhebuterne.marketplace.nms.v1_13_R2.ItemStackReflection
        }

        if (clazzVersion.contains("v1_14_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_14_R1.ItemStackReflection
        }

        if (clazzVersion.contains("v1_15_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_15_R1.ItemStackReflection
        }

        return null
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
