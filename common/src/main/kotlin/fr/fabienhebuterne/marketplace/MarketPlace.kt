package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.config.*
import fr.fabienhebuterne.marketplace.domain.loadInventoryFilterTranslation
import fr.fabienhebuterne.marketplace.domain.loadInventoryLoreTranslation
import fr.fabienhebuterne.marketplace.domain.loadMaterialFilterConfig
import fr.fabienhebuterne.marketplace.domain.loadSkull
import fr.fabienhebuterne.marketplace.exceptions.loadEmptyHandExceptionTranslation
import fr.fabienhebuterne.marketplace.exceptions.loadNotEnoughMoneyExceptionTranslation
import fr.fabienhebuterne.marketplace.listeners.*
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import fr.fabienhebuterne.marketplace.services.ExpirationService
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.MigrationService
import fr.fabienhebuterne.marketplace.services.NotificationService
import fr.fabienhebuterne.marketplace.services.inventory.InventoryOpenedService
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
import fr.fabienhebuterne.marketplace.utils.CommodoreService
import me.lucko.commodore.CommodoreProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.time.Clock

var COLLATION: String? = "utf8mb4_unicode_ci"

class MarketPlace(override var loader: JavaPlugin) : BootstrapLoader {
    private lateinit var callCommandFactoryInit: CallCommandFactoryInit<BootstrapLoader>
    private var econ: Economy? = null
    lateinit var translation: TranslationConfigService
    lateinit var tl: Translation
    lateinit var configService: DefaultConfigService
    lateinit var conf: Config
    lateinit var kodein: DI
    lateinit var instance: MarketPlace
    lateinit var itemStackReflection: IItemStackReflection
    override var isReload: Boolean = false
    override lateinit var missingPermissionMessage: String
    override lateinit var reloadNotAvailableMessage: String

    override fun onEnable() {
        isReload = true
        instance = this

        if (!setupEconomy()) {
            this.loader.logger.severe("Disabled due to no Economy plugin found!")
            this.loader.server.pluginManager.disablePlugin(this.loader)
            return
        }

        configService = DefaultConfigService(this.instance, "config")
        configService.createAndLoadConfig(true)
        conf = configService.getSerialization()

        translation = TranslationConfigService(this.instance, "translation-${conf.language}")
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

        val database = if (conf.database.type == DatabaseType.SQLITE) {
            COLLATION = null
            Database.connect(
                url = "jdbc:sqlite:${this.loader.dataFolder.toPath().resolve("data.db").toUri()}",
                driver = "org.sqlite.JDBC"
            )
        } else {
            Database.connect(
                url = "jdbc:mysql://${conf.database.hostname}:${conf.database.port}/${conf.database.database}?${conf.database.options}",
                driver = "fr.fabienhebuterne.marketplace.libs.mysql.cj.jdbc.Driver",
                user = conf.database.username,
                password = conf.database.password
            )
        }

        transaction {
            SchemaUtils.create(ListingsTable, MailsTable, LogsTable)
            if (conf.database.type != DatabaseType.SQLITE) {
                exec(convertTableToUtf8(ListingsTable))
                exec(convertTableToUtf8(MailsTable))
                exec(convertTableToUtf8(LogsTable))
            }
        }

        initDependencyInjection(database)

        val itemStackReflec: IItemStackReflection by kodein.instance()
        itemStackReflection = itemStackReflec
        loadSkull(itemStackReflection)

        val migrationService: MigrationService by kodein.instance()
        migrationService.migrateAllEntities()

        // TODO : Create factory to init listeners
        registerEvent(InventoryClickEvent::class.java, InventoryClickEventListener(kodein))
        registerEvent(AsyncPlayerChatEvent::class.java, AsyncPlayerChatEventListener(this.instance, kodein))
        registerEvent(PlayerJoinEvent::class.java, PlayerJoinEventListener(kodein))
        registerEvent(InventoryCloseEvent::class.java, InventoryCloseEventListener(kodein))

        // Start tasks to check items expired
        val expirationService: ExpirationService by kodein.instance()
        expirationService.startTaskExpiration()

        if (CommodoreProvider.isSupported()) {
            val marketPlaceCommand = this.loader.getCommand("marketplace")
            CommodoreService().init(this.loader, marketPlaceCommand)
        }

        isReload = false
    }

    // We need to use registerEvent with more parameters because we use generic abstract class to init try catch
    private fun registerEvent(eventClass: Class<out Event>, listener: BaseListener<*>) {
        loader.server.pluginManager.registerEvent(
            eventClass,
            listener,
            EventPriority.NORMAL,
            listener,
            this.loader
        )
    }

    private fun convertTableToUtf8(table: Table): String {
        return "ALTER TABLE " + table.tableName + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    }

    private fun initDependencyInjection(database: Database) {
        val clock = Clock.systemDefaultZone()
        kodein = DI {
            bind<IItemStackReflection>() with singleton { initItemStackNms() }
            bind<ListingsRepository>() with singleton { ListingsRepositoryImpl(instance, database) }
            bind<MailsRepository>() with singleton { MailsRepositoryImpl(instance, database) }
            bind<LogsRepository>() with singleton { LogsRepositoryImpl(instance, database) }
            bind<InventoryOpenedService>() with singleton { InventoryOpenedService() }
            bind<NotificationService>() with singleton { NotificationService(instance) }
            bind<ListingsService>() with singleton { ListingsService(instance, instance(), instance(), clock) }
            bind<MailsService>() with singleton { MailsService(instance, instance()) }
            bind<LogsService>() with singleton { LogsService(instance, instance()) }
            bind<ListingsInventoryService>() with singleton {
                ListingsInventoryService(
                    instance,
                    instance(),
                    instance()
                )
            }
            bind<MailsInventoryService>() with singleton { MailsInventoryService(instance, instance(), instance()) }
            bind<MarketService>() with singleton {
                MarketService(
                    instance,
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance()
                )
            }
            bind<ExpirationService>() with singleton {
                ExpirationService(
                    instance,
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    configService
                )
            }
            bind<MigrationService>() with singleton { MigrationService(instance, instance(), instance(), instance()) }
        }
    }

    private fun initItemStackNms(): IItemStackReflection {
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

        if (clazzVersion.contains("v1_17_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_17_R1.ItemStackReflection
        }

        if (clazzVersion.contains("v1_18_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_18_R1.ItemStackReflection
        }

        if (clazzVersion.contains("v1_18_R2")) {
            return fr.fabienhebuterne.marketplace.nms.v1_18_R2.ItemStackReflection
        }

        if (clazzVersion.contains("v1_19_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_19_R1.ItemStackReflection
        }

        if (clazzVersion.contains("v1_19_R2")) {
            return fr.fabienhebuterne.marketplace.nms.v1_19_R2.ItemStackReflection
        }

        if (clazzVersion.contains("v1_19_R3")) {
            return fr.fabienhebuterne.marketplace.nms.v1_19_R3.ItemStackReflection
        }

        if (clazzVersion.contains("v1_20_R1")) {
            return fr.fabienhebuterne.marketplace.nms.v1_20_R1.ItemStackReflection
        }

        if (clazzVersion.contains("v1_20_R2")) {
            return fr.fabienhebuterne.marketplace.nms.v1_20_R2.ItemStackReflection
        }

        if (clazzVersion.contains("v1_20_R3")) {
            return fr.fabienhebuterne.marketplace.nms.v1_20_R3.ItemStackReflection
        }

        val minecraftVersion: String = Bukkit.getServer().bukkitVersion

        if (minecraftVersion == "1.20.6-R0.1-SNAPSHOT") {
            return fr.fabienhebuterne.marketplace.nms.v1_20_R4.ItemStackReflection
        }

        if (minecraftVersion == "1.21-R0.1-SNAPSHOT" || minecraftVersion == "1.21.1-R0.1-SNAPSHOT") {
            return fr.fabienhebuterne.marketplace.nms.v1_21_R1.ItemStackReflection
        }

        if (minecraftVersion == "1.21.3-R0.1-SNAPSHOT") {
            return fr.fabienhebuterne.marketplace.nms.v1_21_R2.ItemStackReflection
        }

        if (minecraftVersion == "1.21.4-R0.1-SNAPSHOT") {
            return fr.fabienhebuterne.marketplace.nms.v1_21_R3.ItemStackReflection
        }

        if (minecraftVersion == "1.21.5-R0.1-SNAPSHOT") {
            return fr.fabienhebuterne.marketplace.nms.v1_21_R4.ItemStackReflection
        }

        throw IllegalStateException("current server version is not supported by MarketPlace")
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
