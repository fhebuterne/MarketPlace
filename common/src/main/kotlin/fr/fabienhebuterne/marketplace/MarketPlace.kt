package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.config.Config
import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.domain.config.Translation
import fr.fabienhebuterne.marketplace.domain.loadInventoryFilterTranslation
import fr.fabienhebuterne.marketplace.domain.loadInventoryLoreTranslation
import fr.fabienhebuterne.marketplace.domain.loadMaterialFilterConfig
import fr.fabienhebuterne.marketplace.domain.loadSkull
import fr.fabienhebuterne.marketplace.exceptions.loadEmptyHandExceptionTranslation
import fr.fabienhebuterne.marketplace.exceptions.loadNotEnoughMoneyExceptionTranslation
import fr.fabienhebuterne.marketplace.listeners.AsyncPlayerChatEventListener
import fr.fabienhebuterne.marketplace.listeners.InventoryClickEventListener
import fr.fabienhebuterne.marketplace.listeners.PlayerJoinEventListener
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import fr.fabienhebuterne.marketplace.services.ExpirationService
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.MigrationService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.LogsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import fr.fabienhebuterne.marketplace.storage.mysql.*
import fr.fabienhebuterne.marketplace.utils.BootstrapLoader
import fr.fabienhebuterne.marketplace.utils.CustomClassloaderAppender
import fr.fabienhebuterne.marketplace.utils.Dependency
import kotlinx.serialization.UnsafeSerializationApi
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class MarketPlace(override var loader: JavaPlugin) : BootstrapLoader {
    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<BootstrapLoader>
    private var econ: Economy? = null
    lateinit var translation: ConfigService<Translation>
    lateinit var tl: Translation
    lateinit var configService: ConfigService<Config>
    lateinit var conf: Config
    lateinit var kodein: DI
    lateinit var instance: MarketPlace
    lateinit var itemStackReflection: IItemStackReflection
    override var isReload: Boolean = false
    override lateinit var missingPermissionMessage: String
    override lateinit var reloadNotAvailableMessage: String
    var customClassloaderAppender = CustomClassloaderAppender(javaClass.classLoader)

    init {
        val dependency = Dependency(this)
        dependency.downloadDependencies()
        dependency.loadDependencies()
    }

    @UnsafeSerializationApi
    override fun onEnable() {
        isReload = true
        instance = this

        if (!setupEconomy()) {
            this.loader.logger.severe("Disabled due to no Economy plugin found!")
            this.loader.server.pluginManager.disablePlugin(this.loader)
            return
        }

        // TODO : Add method to check missing key/value in current file (compare with resource jar file)
        configService = ConfigService(this.instance, "config", Config::class)
        configService.createAndLoadConfig(true)
        conf = configService.getSerialization()

        translation = ConfigService(this.instance, "translation-fr", Translation::class)
        translation.createAndLoadConfig(true)
        tl = translation.getSerialization()

        missingPermissionMessage = tl.errors.missingPermission
        reloadNotAvailableMessage = tl.errors.reloadNotAvailable

        loadInventoryLoreTranslation(tl.inventoryEnum)
        loadInventoryFilterTranslation(tl.inventoryFilterEnum)
        loadEmptyHandExceptionTranslation(tl.errors.handEmpty)
        loadNotEnoughMoneyExceptionTranslation(tl.errors.notEnoughMoney)
        loadMaterialFilterConfig(conf.inventoryLoreMaterial.filter)

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

        initDependencyInjection(database)

        val itemStackReflec: IItemStackReflection by kodein.instance()
        itemStackReflection = itemStackReflec
        loadSkull(itemStackReflection)

        val migrationService: MigrationService by kodein.instance()
        migrationService.migrateAllEntities()

        // TODO : Create factory to init listeners
        loader.server.pluginManager.registerEvents(InventoryClickEventListener(this.instance, kodein), this.loader)
        loader.server.pluginManager.registerEvents(AsyncPlayerChatEventListener(this.instance, kodein), this.loader)
        loader.server.pluginManager.registerEvents(PlayerJoinEventListener(kodein), this.loader)

        // Start tasks to check items expired
        val expirationService: ExpirationService by kodein.instance()
        expirationService.startTaskExpirationListingsToMails()
        expirationService.startTaskExpirationMailsToDelete()

        isReload = false
    }

    private fun initDependencyInjection(database: Database) {
        kodein = DI {
            bind<IItemStackReflection>() with singleton { initItemStackNms() ?: throw Exception() }
            bind<ListingsRepository>() with singleton { ListingsRepositoryImpl(instance, database) }
            bind<MailsRepository>() with singleton { MailsRepositoryImpl(instance, database) }
            bind<LogsRepository>() with singleton { LogsRepositoryImpl(instance, database) }
            bind<ListingsService>() with singleton { ListingsService(instance, instance(), instance()) }
            bind<MailsService>() with singleton { MailsService(instance, instance()) }
            bind<LogsService>() with singleton { LogsService(instance, instance()) }
            bind<ListingsInventoryService>() with singleton { ListingsInventoryService(instance, instance()) }
            bind<MailsInventoryService>() with singleton { MailsInventoryService(instance, instance()) }
            bind<MarketService>() with singleton {
                MarketService(
                    instance,
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance()
                )
            }
            bind<ExpirationService>() with singleton { ExpirationService(instance, instance(), instance(), instance()) }
            bind<MigrationService>() with singleton { MigrationService(instance, instance(), instance(), instance()) }
        }
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

        if (clazzVersion.contains("v1_16_R3")) {
            return fr.fabienhebuterne.marketplace.nms.v1_16_R3.ItemStackReflection
        }

        return null
    }

    override fun onDisable() {
        // No method on disable
    }

    override fun onLoad() {
        // No method on load
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        commandLabel: String,
        args: Array<String>
    ): Boolean {
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
        if (this.loader.server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy> =
            this.loader.server.servicesManager.getRegistration(Economy::class.java)
                ?: return false
        econ = rsp.provider
        return econ != null
    }

    fun getEconomy(): Economy {
        return econ ?: throw Exception("Cannot found economy plugin...")
    }
}
