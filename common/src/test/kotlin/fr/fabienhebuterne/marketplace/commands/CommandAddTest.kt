package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.exceptions.loadEmptyHandExceptionTranslation
import fr.fabienhebuterne.marketplace.initItemStackMock
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import io.mockk.*
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.text.MessageFormat

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

    @Test
    fun `should player cannot use add command when money is not a double valid`() {
        // GIVEN
        val money = "DoubleNotValid"
        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.add") } returns true
        every { marketPlace.isReload } returns false
        every { playerMock.inventory.itemInMainHand.type } returns Material.DIRT
        every { playerMock.sendMessage(MessageFormat.format(translation.errors.numberNotValid, money)) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("add", money),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.add")
            playerMock.sendMessage(MessageFormat.format(translation.errors.numberNotValid, money))
        }
    }

    @Test
    fun `should player cannot use add command when money is superior than limit in config`() {
        // GIVEN
        val money = "100000000000000.0"
        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.add") } returns true
        every { marketPlace.isReload } returns false
        every { playerMock.inventory.itemInMainHand.type } returns Material.DIRT
        every { playerMock.sendMessage(MessageFormat.format(translation.errors.numberTooBig, money)) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("add", money),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.add")
            playerMock.sendMessage(MessageFormat.format(translation.errors.numberTooBig, money))
        }
    }

    @Test
    fun `should player add new item in listings with add command`() {
        // GIVEN
        val money = 100.0
        val inventory: Inventory = mockk()
        val inventoryView: InventoryView = mockk()
        val playerInventory: PlayerInventory = mockk()
        val secondItemStack: ItemStack = initItemStackMock(Material.DIRT, 1, null, false)
        every { secondItemStack.setAmount(1) } just Runs
        val itemStack: ItemStack = initItemStackMock(Material.DIRT, 10, null, false)
        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.add") } returns true
        every { marketPlace.isReload } returns false
        every { marketPlace.itemStackReflection.getVersion() } returns 1343
        every { playerMock.inventory } returns playerInventory
        every { playerInventory.itemInMainHand } returns itemStack
        every { playerInventory.itemInMainHand.amount } returns 10
        every { playerInventory.itemInMainHand.type } returns itemStack.type
        every { playerInventory.itemInMainHand.clone() } returns secondItemStack
        every { listingsRepositoryMock.find(playerMock.uniqueId, secondItemStack, money) } returns null
        every { listingsInventoryServiceMock.confirmationAddNewItem(playerMock, any()) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("add", money.toString()),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.add")
            listingsRepositoryMock.find(playerMock.uniqueId, secondItemStack, money)
            listingsInventoryServiceMock.confirmationAddNewItem(playerMock, any())
            playerMock.openInventory(inventory)
        }
    }

    @Test
    fun `should player update existing item in listings with add command`() {
        // GIVEN
        val money = 100.0
        val playerInventory: PlayerInventory = mockk()
        val secondItemStack: ItemStack = initItemStackMock(Material.DIRT, 1, null, false)
        every { secondItemStack.setAmount(1) } just Runs
        val itemStack: ItemStack = initItemStackMock(Material.DIRT, 24, null, false)

        val listings = Listings(
            sellerUuid = fabienUuid,
            sellerPseudo = "Fabien91",
            itemStack = secondItemStack,
            quantity = itemStack.amount,
            price = money,
            world = "world",
            auditData = AuditData(createdAt = System.currentTimeMillis()),
            version = 1343
        )

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.add") } returns true
        every { marketPlace.isReload } returns false
        every { marketPlace.itemStackReflection.getVersion() } returns 1343
        every { playerMock.inventory } returns playerInventory
        every { playerInventory.itemInMainHand } returns itemStack
        every { playerInventory.itemInMainHand.amount } returns 24
        every { playerInventory.itemInMainHand.type } returns itemStack.type
        every { playerInventory.itemInMainHand.clone() } returns secondItemStack
        every { listingsRepositoryMock.find(playerMock.uniqueId, secondItemStack, money) } returns listings
        every { listingsServiceMock.updateListings(listings, 24, playerMock) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("add", money.toString()),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.add")
            listingsRepositoryMock.find(playerMock.uniqueId, secondItemStack, money)
            listingsServiceMock.updateListings(listings, 24, playerMock)
        }
    }
}
