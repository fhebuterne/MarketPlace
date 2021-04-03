package fr.fabienhebuterne.marketplace

import fr.fabienhebuterne.marketplace.domain.config.ConfigService
import fr.fabienhebuterne.marketplace.domain.config.Translation
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.UnsafeSerializationApi
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {

    private val resource: URL = this::class.java.classLoader.getResource("loader")
        ?: throw IllegalAccessException("ressource path not exist")
    private val filepath: Path = Paths.get(resource.toURI())
    private val javaPluginMock: JavaPlugin = mockk()
    private val serverMock: Server = mockk()
    val marketPlace: MarketPlace = mockk()

    lateinit var translation: Translation
    val fabienUuid = UUID.fromString("522841e6-a3b6-48dd-b67c-0b0f06ec1aa6")

    @BeforeAll
    @UnsafeSerializationApi
    fun init() {
        every { marketPlace.loader } returns javaPluginMock
        every { marketPlace.loader.dataFolder } returns filepath.toFile()
        every { marketPlace.loader.server } returns serverMock

        val configService = ConfigService(marketPlace, "translation-fr", Translation::class)
        configService.loadConfig()
        translation = configService.getSerialization()

        every { marketPlace.tl } returns configService.getSerialization()
    }

}
