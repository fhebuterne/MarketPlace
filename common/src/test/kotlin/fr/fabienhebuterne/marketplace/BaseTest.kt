package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.domain.config.Translation
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.UnsafeSerializationApi
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.BeforeEach
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

abstract class BaseTest {

    private val resource: URL = this::class.java.classLoader.getResource("loader")
        ?: throw IllegalAccessException("ressource path not exist")
    private val filepath: Path = Paths.get(resource.toURI())
    val serverMock: Server = mockk()
    val javaPluginMock: JavaPlugin = mockk()
    val marketPlace: MarketPlace = mockk()
    var playerMock: Player = mockk()

    lateinit var translation: Translation
    val fabienUuid: UUID = UUID.fromString("522841e6-a3b6-48dd-b67c-0b0f06ec1aa6")
    val ergailUuid: UUID = UUID.fromString("4a109300-ec09-4c47-9e8d-de735dd7f17f")

    @UnsafeSerializationApi
    @BeforeEach
    fun initDefault() {
        every { marketPlace.loader } returns javaPluginMock
        every { marketPlace.loader.dataFolder } returns filepath.toFile()
        every { marketPlace.loader.server } returns serverMock

        val configService = ConfigService(marketPlace, "translation-fr", Translation::class)
        configService.loadConfig()
        translation = configService.getSerialization()

        every { marketPlace.tl } returns configService.getSerialization()
        every { marketPlace.missingPermissionMessage } returns translation.errors.missingPermission
        every { marketPlace.reloadNotAvailableMessage } returns translation.errors.reloadNotAvailable
    }

    @BeforeEach
    fun initPlayerMock() {
        // Reset this mock on each test
        playerMock = mockk()
        every { playerMock.uniqueId } returns fabienUuid
    }

}
