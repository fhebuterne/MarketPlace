package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import io.mockk.*
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class CommandMailsTest : BaseTest() {

    private val commandMailPermission = "marketplace.mails"
    private val commandMailOtherPermission = "marketplace.mails.other"
    private val command: Command = mockk()
    private val mailsServiceMock: MailsService = mockk()
    private val mailsInventoryService: MailsInventoryService = mockk()
    private val kodein = DI {
        bind<MailsService>() with singleton { mailsServiceMock }
        bind<MailsInventoryService>() with singleton { mailsInventoryService }
    }

    @Test
    fun `should open mails inventory as a player with mails command`() {
        // GIVEN
        val pagination = Pagination<Mails>(
            currentPlayer = playerMock.uniqueId,
            viewPlayer = playerMock.uniqueId
        )
        val inventory: Inventory = mockk()
        val inventoryView: InventoryView = mockk()

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission(commandMailPermission) } returns true
        every { marketPlace.isReload } returns false
        every { mailsServiceMock.getPaginated(pagination = pagination) } returns pagination
        every { mailsInventoryService.initInventory(pagination, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            commandLabel,
            arrayOf("mails"),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission(commandMailPermission)
            mailsServiceMock.getPaginated(pagination = pagination)
            mailsInventoryService.initInventory(pagination, playerMock)
            playerMock.openInventory(inventory)
        }
    }

    @Test
    fun `should cannot open mails inventory as an admin when missing permission with mails command`() {
        // GIVEN
        val playerName = "Ergail"
        val offlinePlayer: OfflinePlayer = mockk()
        every { offlinePlayer.uniqueId } returns ergailUuid

        every { command.aliases } returns arrayListOf()
        every { marketPlace.isReload } returns false
        every { playerMock.hasPermission(commandMailPermission) } returns true
        every { playerMock.hasPermission(commandMailOtherPermission) } returns false
        every { playerMock.sendMessage(translation.errors.missingPermission) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            commandLabel,
            arrayOf("mails", playerName),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission(commandMailPermission)
            playerMock.hasPermission(commandMailOtherPermission)
            playerMock.sendMessage(translation.errors.missingPermission)
        }
    }

    @Test
    fun `should open mails inventory as an admin with mails command`() {
        // GIVEN
        val inventory: Inventory = mockk()
        val inventoryView: InventoryView = mockk()
        val playerName = "Ergail"
        val offlinePlayer: OfflinePlayer = mockk()
        every { offlinePlayer.uniqueId } returns ergailUuid
        val pagination = Pagination<Mails>(
            currentPlayer = offlinePlayer.uniqueId,
            viewPlayer = playerMock.uniqueId
        )

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission(commandMailPermission) } returns true
        every { playerMock.hasPermission(commandMailOtherPermission) } returns true
        every { mailsServiceMock.findUuidByPseudo(playerName) } returns ergailUuid
        every { marketPlace.isReload } returns false
        every { mailsServiceMock.getPaginated(pagination = pagination) } returns pagination
        every { mailsInventoryService.initInventory(pagination, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            commandLabel,
            arrayOf("mails", playerName),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission(commandMailPermission)
            playerMock.hasPermission(commandMailOtherPermission)
            mailsServiceMock.findUuidByPseudo(playerName)
            mailsServiceMock.getPaginated(pagination = pagination)
            mailsInventoryService.initInventory(pagination, playerMock)
            playerMock.openInventory(inventory)
        }
    }

    @Test
    fun `should open mails inventory as an admin with uuid in argument with mails command`() {
        // GIVEN
        val inventory: Inventory = mockk()
        val inventoryView: InventoryView = mockk()
        val offlinePlayer: OfflinePlayer = mockk()
        every { offlinePlayer.uniqueId } returns ergailUuid
        val pagination = Pagination<Mails>(
            currentPlayer = offlinePlayer.uniqueId,
            viewPlayer = playerMock.uniqueId
        )

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission(commandMailPermission) } returns true
        every { playerMock.hasPermission(commandMailOtherPermission) } returns true
        every { marketPlace.isReload } returns false
        every { mailsServiceMock.getPaginated(pagination = pagination) } returns pagination
        every { mailsInventoryService.initInventory(pagination, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            commandLabel,
            arrayOf("mails", ergailUuid.toString()),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission(commandMailPermission)
            playerMock.hasPermission(commandMailOtherPermission)
            mailsServiceMock.getPaginated(pagination = pagination)
            mailsInventoryService.initInventory(pagination, playerMock)
            playerMock.openInventory(inventory)
        }
    }

    @Test
    fun `should cannot open mails inventory as an admin when uuid not exist with mails command`() {
        // GIVEN
        val playerName = "truc"

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission(commandMailPermission) } returns true
        every { playerMock.hasPermission(commandMailOtherPermission) } returns true
        every { marketPlace.isReload } returns false
        every { mailsServiceMock.findUuidByPseudo(playerName) } returns null

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            commandLabel,
            arrayOf("mails", playerName),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission(commandMailPermission)
            playerMock.hasPermission(commandMailOtherPermission)
            mailsServiceMock.findUuidByPseudo(playerName)
        }

        verify(exactly = 0) {
            playerMock.openInventory(any<Inventory>())
        }
    }

}
