package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.exceptions.loadEmptyHandExceptionTranslation
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import io.mockk.*
import org.bukkit.Material
import org.bukkit.command.Command
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class CommandAddTest : BaseTest() {

    private val command: Command = mockk()
    private val listingsRepositoryMock: ListingsRepository = mockk()
    private val listingsServiceMock: ListingsService = mockk()
    private val listingsInventoryServiceMock: ListingsInventoryService = mockk()

    private val kodein = DI {
        bind<ListingsRepository>() with singleton { listingsRepositoryMock }
        bind<ListingsService>() with singleton { listingsServiceMock }
        bind<ListingsInventoryService>() with singleton { listingsInventoryServiceMock }
    }

    @Test
    fun `should player cannot use add command with air material`() {
        // GIVEN
        loadEmptyHandExceptionTranslation(translation.errors.handEmpty)

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.add") } returns true
        every { marketPlace.isReload } returns false
        every { playerMock.inventory.itemInMainHand.type } returns Material.AIR
        every { playerMock.sendMessage(translation.errors.handEmpty) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("add"),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.add")
            playerMock.sendMessage(translation.errors.handEmpty)
        }
    }

    @Test
    fun `should player cannot use add command when missing arguments`() {
        // GIVEN
        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.add") } returns true
        every { marketPlace.isReload } returns false
        every { playerMock.inventory.itemInMainHand.type } returns Material.DIRT
        every { playerMock.sendMessage(translation.commandAddUsage) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("add"),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.add")
            playerMock.sendMessage(translation.commandAddUsage)
        }
    }

}
