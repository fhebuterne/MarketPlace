package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class CommandMailsTest : BaseTest() {

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
        every { playerMock.hasPermission("marketplace.mails") } returns true
        every { marketPlace.isReload } returns false
        every { mailsServiceMock.getPaginated(pagination = pagination) } returns pagination
        every { mailsInventoryService.initInventory(pagination, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("mails"),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.mails")
            mailsServiceMock.getPaginated(pagination = pagination)
            mailsInventoryService.initInventory(pagination, playerMock)
            playerMock.openInventory(inventory)
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
        every { playerMock.hasPermission("marketplace.mails") } returns true
        every { playerMock.hasPermission("marketplace.mails.other") } returns true
        every { marketPlace.isReload } returns false
        every { mailsServiceMock.getPaginated(pagination = pagination) } returns pagination
        every { mailsInventoryService.initInventory(pagination, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView
        every { Bukkit.getOfflinePlayer(playerName) } returns offlinePlayer

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("mails", playerName),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.mails")
            playerMock.hasPermission("marketplace.mails.other")
            mailsServiceMock.getPaginated(pagination = pagination)
            mailsInventoryService.initInventory(pagination, playerMock)
            playerMock.openInventory(inventory)
        }
    }

}