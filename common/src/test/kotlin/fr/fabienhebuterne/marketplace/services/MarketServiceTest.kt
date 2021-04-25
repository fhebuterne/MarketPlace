package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.exceptions.NotEnoughMoneyException
import fr.fabienhebuterne.marketplace.exceptions.loadNotEnoughMoneyExceptionTranslation
import fr.fabienhebuterne.marketplace.initItemStackMock
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import io.mockk.*
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isFailure
import java.util.*

class MarketServiceTest : BaseTest() {

    private val listingsService: ListingsService = mockk()
    private val listingsRepository: ListingsRepository = mockk()
    private val listingsInventoryService: ListingsInventoryService = mockk()
    private val mailsService: MailsService = mockk()
    private val mailsRepository: MailsRepository = mockk()
    private val mailsInventoryService: MailsInventoryService = mockk()
    private val logsService: LogsService = mockk()
    private val notificationService: NotificationService = mockk()
    private val fabienOfflinePlayer: OfflinePlayer = mockk()
    private val ergailOfflinePlayer: OfflinePlayer = mockk()

    private val marketService: MarketService = MarketService(
        marketPlace,
        listingsService,
        listingsRepository,
        listingsInventoryService,
        mailsService,
        mailsRepository,
        mailsInventoryService,
        logsService,
        notificationService
    )

    private fun initListings(quantity: Int = 31): Pagination<Listings> {
        val itemStack: ItemStack = mockk()
        every { itemStack.type } returns Material.DIRT

        val listings = mutableMapOf(
            Pair(
                fabienUuid,
                Pagination(
                    results = listOf(
                        Listings(
                            id = UUID.randomUUID(),
                            auditData = AuditData(createdAt = System.currentTimeMillis()),
                            itemStack = itemStack,
                            quantity = quantity,
                            price = 10.0,
                            sellerPseudo = "Ergail",
                            sellerUuid = ergailUuid,
                            world = "world",
                            version = 1343
                        )
                    ),
                    currentPlayer = fabienUuid,
                    viewPlayer = fabienUuid
                )
            )
        )

        every { listingsService.playersView } returns listings
        every { Bukkit.getOfflinePlayer(fabienUuid) } returns fabienOfflinePlayer
        every { Bukkit.getOfflinePlayer(ergailUuid) } returns ergailOfflinePlayer

        return listings[fabienUuid] ?: throw IllegalAccessException("listings not found")
    }

    private fun initMails(quantity: Int = 31): Pagination<Mails> {
        val itemStack = initItemStackMock(Material.DIRT, 1, null, false)

        val mails = mutableMapOf(
            Pair(
                fabienUuid,
                Pagination(
                    results = listOf(
                        Mails(
                            id = UUID.randomUUID(),
                            playerPseudo = "Fabien91",
                            playerUuid = fabienUuid,
                            itemStack = itemStack,
                            quantity = quantity,
                            auditData = AuditData(createdAt = System.currentTimeMillis()),
                            version = 1343
                        )
                    ),
                    currentPlayer = fabienUuid,
                    viewPlayer = fabienUuid
                )
            )
        )

        every { mailsService.playersView } returns mails
        every { Bukkit.getOfflinePlayer(fabienUuid) } returns fabienOfflinePlayer
        every { Bukkit.getOfflinePlayer(ergailUuid) } returns ergailOfflinePlayer

        return mails[fabienUuid] ?: throw IllegalAccessException("mails not found")
    }

    @Test
    fun `should player cannot buy item if quantity is not sufficient`() {
        // GIVEN
        initListings(1)
        every { playerMock.sendMessage(translation.errors.quantityNotAvailable) } just Runs

        // WHEN
        marketService.buyItem(playerMock, 0, 5, true)

        // THEN
        verify(exactly = 0) {
            listingsRepository.find(any(), any(), any())
        }

        verify(exactly = 1) {
            playerMock.sendMessage(translation.errors.quantityNotAvailable)
        }
    }

    @Test
    fun `should player cannot buy item if item selected no longer exist`() {
        // GIVEN
        val playerView = initListings()
        val listings: Listings = playerView.results[0]
        every { playerMock.sendMessage(translation.errors.itemNotExist) } just Runs
        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns null

        // WHEN
        marketService.buyItem(playerMock, 0, 1, false)

        // THEN
        verify(exactly = 1) {
            listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)
            playerMock.sendMessage(translation.errors.itemNotExist)
        }
    }

    @Test
    fun `should player cannot buy item if quantity in DB is not sufficient`() {
        // GIVEN
        val playerView = initListings()
        val listings: Listings = playerView.results[0]
        val listingsQte = listings.copy(quantity = 30)
        every { playerMock.sendMessage(translation.errors.quantityNotAvailable) } just Runs
        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns listingsQte

        // WHEN
        marketService.buyItem(playerMock, 0, 31, false)

        // THEN
        verify(exactly = 1) {
            listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)
            playerMock.sendMessage(translation.errors.quantityNotAvailable)
        }
    }

    @Test
    fun `should player cannot buy item if his money is not sufficient`() {
        // GIVEN
        val playerView = initListings()
        val quantity = 30
        val listings: Listings = playerView.results[0]
        val listingsQte = listings.copy(quantity = quantity)
        val money = listings.price * quantity

        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns listingsQte
        every { marketPlace.getEconomy().has(fabienOfflinePlayer, money) } returns false
        loadNotEnoughMoneyExceptionTranslation(translation.errors.notEnoughMoney)
        every { playerMock.sendMessage(translation.errors.notEnoughMoney) } just Runs

        // WHEN
        expectCatching {
            marketService.buyItem(playerMock, 0, 30, false)
        }.isFailure().isA<NotEnoughMoneyException>()

        // THEN
        verify(exactly = 1) {
            listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)
            playerMock.sendMessage(translation.errors.notEnoughMoney)
        }
    }

    @Test
    fun `should player can buy an item when quantity is inferior than sell`() {
        this.`should player can buy an item when quantity is equal or inferior than sell`(5, 30)
    }

    @Test
    fun `should player can buy an item when quantity is equal than sell`() {
        this.`should player can buy an item when quantity is equal or inferior than sell`(40, 40)
    }

    private fun `should player can buy an item when quantity is equal or inferior than sell`(
        quantity: Int,
        quantityDb: Int
    ) {
        // GIVEN
        val playerView = initListings(quantityDb)
        val listings: Listings = playerView.results[0]
        val money = listings.price * quantity
        val economyResponse = EconomyResponse(100.0, 100.0, EconomyResponse.ResponseType.SUCCESS, "")
        val inventory: Inventory = mockk()
        val economy: Economy = mockk()
        val bukkitScheduler: BukkitScheduler = mockk()
        val bukkitTask: BukkitTask = mockk()
        val runnableSlot = slot<Runnable>()
        val loader = marketPlace.loader
        val finalMessage = "§8[§6MarketPlace§8] §aVous venez d'acheter ${quantity}xDIRT pour ${money}$."

        every { Bukkit.getScheduler() } returns bukkitScheduler
        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns listings
        every { marketPlace.getEconomy() } returns economy
        every { economy.has(fabienOfflinePlayer, money) } returns true
        every { economy.withdrawPlayer(fabienOfflinePlayer, money) } returns economyResponse
        every { economy.depositPlayer(ergailOfflinePlayer, money) } returns economyResponse
        if (quantity == quantityDb) {
            every { listingsRepository.delete(listings.id!!) } just Runs
        } else {
            every { listingsRepository.update(listings.copy(quantity = quantityDb - quantity)) } returns listings.copy(
                quantity = quantityDb - quantity
            )
        }
        every { mailsService.saveListingsToMail(listings, playerMock, quantity) } just Runs
        every { logsService.buyItemLog(playerMock, listings, quantity, money) } just Runs
        every { notificationService.sellerItemNotification(listings, quantity, money) } just Runs
        every { playerMock.sendMessage(finalMessage) } just Runs
        every { listingsService.getPaginated(pagination = playerView) } returns playerView
        every { listingsInventoryService.initInventory(playerView, playerMock) } returns inventory
        every { listingsInventoryService.openInventory(playerMock, inventory) } just Runs
        every { bukkitScheduler.runTask(loader, capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            bukkitTask
        }

        // WHEN
        marketService.buyItem(playerMock, 0, quantity, false)

        // THEN
        verify(exactly = 1) {
            listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)
            economy.has(fabienOfflinePlayer, money)
            economy.withdrawPlayer(fabienOfflinePlayer, money)
            economy.depositPlayer(ergailOfflinePlayer, money)
            if (quantity == quantityDb) {
                listingsRepository.delete(listings.id!!)
            } else {
                listingsRepository.update(listings.copy(quantity = quantityDb - quantity))
            }
            mailsService.saveListingsToMail(listings, playerMock, quantity)
            logsService.buyItemLog(playerMock, listings, quantity, money)
            notificationService.sellerItemNotification(listings, quantity, money)
            playerMock.sendMessage(finalMessage)
            listingsService.getPaginated(pagination = playerView)
            listingsInventoryService.initInventory(playerView, playerMock)
            bukkitScheduler.runTask(loader, runnableSlot.captured)
            listingsInventoryService.openInventory(playerMock, inventory)
        }
    }

    @Test
    fun `should cannot take an item from mails to inventory when is air or null`() {
        // GIVEN
        val itemStack: ItemStack = mockk()
        every { itemStack.type } returns Material.DIRT

        val inventoryClickEvent: InventoryClickEvent = mockk()
        every { inventoryClickEvent.rawSlot } returns 0
        every { inventoryClickEvent.currentItem } returns null

        // WHEN
        marketService.clickOnMailsInventory(inventoryClickEvent, playerMock)

        // THEN
        verify(exactly = 0) {
            mailsService.playersView[playerMock.uniqueId]
        }
    }

    @Test
    fun `should normal player can take limited items quantity from mails to inventory`() {
        // GIVEN
        val inventory: Inventory = mockk()
        val playerInventory: PlayerInventory = mockk()
        val playerView = initMails(200)
        val mails: Mails = playerView.results[0]
        every { mails.itemStack.clone() } returns mails.itemStack

        val itemStackDirtInInventory = initItemStackMock(Material.DIRT, 62, mails.itemStack, true)

        val inventoryClickEvent: InventoryClickEvent = mockk()
        every { inventoryClickEvent.rawSlot } returns 0
        every { inventoryClickEvent.currentItem } returns mails.itemStack
        every { inventoryClickEvent.isShiftClick } returns false
        every { inventoryClickEvent.isLeftClick } returns true
        every { playerMock.inventory } returns playerInventory
        every { playerInventory.storageContents } returns arrayOf(itemStackDirtInInventory, null, null)
        every { playerInventory.addItem(mails.itemStack) } returns hashMapOf()

        every { mailsService.find(mails.id.toString()) } returns mails
        every { logsService.takeItemLog(playerMock, mails, 130, false) } just Runs
        every { mailsRepository.update(mails.copy(quantity = 70)) } returns mails.copy(quantity = 70)
        every { mailsService.getPaginated(pagination = playerView) } returns playerView
        every { mailsInventoryService.initInventory(playerView, playerMock) } returns inventory
        every { mailsInventoryService.openInventory(playerMock, inventory) } just Runs

        // WHEN
        marketService.clickOnMailsInventory(inventoryClickEvent, playerMock)

        // THEN
        verify(exactly = 130) {
            playerInventory.addItem(mails.itemStack)
        }

        verify(exactly = 1) {
            mailsService.find(mails.id.toString())
            logsService.takeItemLog(playerMock, mails, 130, false)
            mailsRepository.update(mails.copy(quantity = 70))
            mailsService.getPaginated(pagination = playerView)
            mailsInventoryService.initInventory(playerView, playerMock)
            mailsInventoryService.openInventory(playerMock, inventory)
        }
    }

    @Test
    fun `should player can buy one item with left click from listings inventory click`() {
        // GIVEN
        val inventoryClickEvent: InventoryClickEvent = mockk()
        val inventory: Inventory = mockk()
        val playerView = initListings(100)
        val listings: Listings = playerView.results[0]
        every { listings.itemStack.clone() } returns listings.itemStack
        val quantity = 1
        val money = listings.price * quantity
        val economyResponse = EconomyResponse(100.0, 100.0, EconomyResponse.ResponseType.SUCCESS, "")
        val economy: Economy = mockk()
        val bukkitScheduler: BukkitScheduler = mockk()
        val bukkitTask: BukkitTask = mockk()
        val runnableSlot = slot<Runnable>()
        val loader = marketPlace.loader
        val finalMessage = "§8[§6MarketPlace§8] §aVous venez d'acheter ${quantity}xDIRT pour ${money}$."

        every { inventoryClickEvent.rawSlot } returns 0
        every { inventoryClickEvent.currentItem } returns listings.itemStack
        every { inventoryClickEvent.isShiftClick } returns false
        every { inventoryClickEvent.isLeftClick } returns true
        every { inventoryClickEvent.click } returns ClickType.LEFT

        every { Bukkit.getScheduler() } returns bukkitScheduler
        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns listings
        every { marketPlace.getEconomy() } returns economy
        every { economy.has(fabienOfflinePlayer, money) } returns true
        every { economy.withdrawPlayer(fabienOfflinePlayer, money) } returns economyResponse
        every { economy.depositPlayer(ergailOfflinePlayer, money) } returns economyResponse
        every { listingsRepository.update(listings.copy(quantity = 100 - quantity)) } returns listings.copy(
            quantity = 100 - quantity
        )
        every { mailsService.saveListingsToMail(listings, playerMock, quantity) } just Runs
        every { logsService.buyItemLog(playerMock, listings, quantity, money) } just Runs
        every { notificationService.sellerItemNotification(listings, quantity, money) } just Runs
        every { playerMock.sendMessage(finalMessage) } just Runs
        every { listingsService.getPaginated(pagination = playerView) } returns playerView
        every { listingsInventoryService.initInventory(playerView, playerMock) } returns inventory
        every { listingsInventoryService.openInventory(playerMock, inventory) } just Runs
        every { bukkitScheduler.runTask(loader, capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            bukkitTask
        }

        every { playerMock.hasPermission("marketplace.listings.other.remove") } returns false

        // WHEN
        marketService.playersWaitingDefinedQuantity[playerMock.uniqueId] =
            WaitingDefinedQuantity(listings, ClickType.LEFT)
        marketService.clickOnListingsInventory(inventoryClickEvent, playerMock)

        // THEN
        verify(exactly = 1) {
            listingsRepository.update(listings.copy(quantity = 99))
            listingsInventoryService.openInventory(playerMock, inventory)
        }

        expectThat(marketService.playersWaitingDefinedQuantity).isEmpty()
    }
}
