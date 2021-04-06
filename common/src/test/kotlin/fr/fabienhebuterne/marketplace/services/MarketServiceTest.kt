package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.exceptions.NotEnoughMoneyException
import fr.fabienhebuterne.marketplace.exceptions.loadNotEnoughMoneyExceptionTranslation
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
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemFactory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isA
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
    private var playerMock: Player = mockk()

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

    private fun initPlayerView(quantity: Int = 31): Pagination<Listings> {
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
                            sellerUuid = UUID.fromString("4a109300-ec09-4c47-9e8d-de735dd7f17f"),
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

        return listings[fabienUuid] ?: throw IllegalAccessException("listing not found")
    }

    @BeforeEach
    fun initItemStack() {
        // Mockk only for itemStack
        mockkStatic(Bukkit::class)
        val itemFactory: ItemFactory = mockk()
        every { Bukkit.getItemFactory() } returns itemFactory
        every { itemFactory.equals(null, null) } returns false
        every { itemFactory.getItemMeta(Material.DIRT) } returns null
    }

    @BeforeEach
    fun initPlayerMock() {
        // Reset this mock on each test
        playerMock = mockk()
        every { playerMock.uniqueId } returns fabienUuid
    }

    @Test
    fun `should player cannot buy item if quantity is not sufficient`() {
        // GIVEN
        initPlayerView(1)
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
        val playerView = initPlayerView()
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
        val playerView = initPlayerView()
        val listings: Listings = playerView.results[0]
        every { playerMock.sendMessage(translation.errors.quantityNotAvailable) } just Runs
        every {
            listingsRepository.find(
                listings.sellerUuid,
                listings.itemStack,
                listings.price
            )
        } returns listings.copy(quantity = 30)

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
        val playerView = initPlayerView()
        val listings: Listings = playerView.results[0]
        val quantity = 30
        val money = listings.price * quantity
        val offlinePlayer: OfflinePlayer = mockk()
        every { Bukkit.getOfflinePlayer(fabienUuid) } returns offlinePlayer
        every {
            listingsRepository.find(
                listings.sellerUuid,
                listings.itemStack,
                listings.price
            )
        } returns listings.copy(quantity = quantity)
        every { marketPlace.getEconomy().has(offlinePlayer, money) } returns false
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
        // GIVEN
        val playerView = initPlayerView()
        val listings: Listings = playerView.results[0]
        val quantity = 5
        val listingsDb = listings.copy(quantity = 30)
        val money = listings.price * quantity
        val economyResponse = EconomyResponse(100.0, 100.0, EconomyResponse.ResponseType.SUCCESS, "")
        val inventory: Inventory = mockk()
        val inventoryView: InventoryView = mockk()
        val offlinePlayer: OfflinePlayer = mockk()
        val economy: Economy = mockk()

        val finalMessage = "§8[§6MarketPlace§8] §aVous venez d'acheter 5xDIRT pour 50.0$."

        every { Bukkit.getOfflinePlayer(fabienUuid) } returns offlinePlayer
        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns listingsDb
        every { marketPlace.getEconomy() } returns economy
        every { economy.has(offlinePlayer, money) } returns true
        every { economy.withdrawPlayer(offlinePlayer, money) } returns economyResponse
        every { economy.depositPlayer(offlinePlayer, money) } returns economyResponse
        every { listingsRepository.update(listings.copy(quantity = 25)) } returns listings.copy(quantity = 25)
        every { mailsService.saveListingsToMail(listingsDb, playerMock, quantity) } just Runs
        every { logsService.buyItemLog(playerMock, listingsDb, quantity, money) } just Runs
        every { notificationService.sellerItemNotification(listingsDb, quantity, money) } just Runs
        every { playerMock.sendMessage(finalMessage) } just Runs
        every { listingsService.getPaginated(pagination = playerView) } returns playerView
        every { listingsInventoryService.initInventory(playerView, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        marketService.buyItem(playerMock, 0, quantity, false)

        // THEN
        verify(exactly = 1) {
            listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)
            economy.has(offlinePlayer, money)
            economy.withdrawPlayer(offlinePlayer, money)
            economy.depositPlayer(offlinePlayer, money)
            listingsRepository.update(listings.copy(quantity = 25))
            mailsService.saveListingsToMail(listingsDb, playerMock, quantity)
            logsService.buyItemLog(playerMock, listingsDb, quantity, money)
            notificationService.sellerItemNotification(listingsDb, quantity, money)
            playerMock.sendMessage(finalMessage)
            listingsService.getPaginated(pagination = playerView)
            listingsInventoryService.initInventory(playerView, playerMock)
            playerMock.openInventory(inventory)
        }
    }

    @Test
    fun `should player can buy an item when quantity is equal than sell`() {
        // GIVEN
        val playerView = initPlayerView()
        val listings: Listings = playerView.results[0]
        val quantity = 30
        val listingsDb = listings.copy(quantity = 30)
        val money = listings.price * quantity
        val economyResponse = EconomyResponse(100.0, 100.0, EconomyResponse.ResponseType.SUCCESS, "")
        val inventory: Inventory = mockk()
        val inventoryView: InventoryView = mockk()
        val offlinePlayer: OfflinePlayer = mockk()
        val economy: Economy = mockk()

        val finalMessage = "§8[§6MarketPlace§8] §aVous venez d'acheter 30xDIRT pour 300.0$."

        every { Bukkit.getOfflinePlayer(fabienUuid) } returns offlinePlayer
        every { listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price) } returns listingsDb
        every { marketPlace.getEconomy() } returns economy
        every { economy.has(offlinePlayer, money) } returns true
        every { economy.withdrawPlayer(offlinePlayer, money) } returns economyResponse
        every { economy.depositPlayer(offlinePlayer, money) } returns economyResponse
        every { listingsRepository.delete(listings.id!!) } just Runs
        every { mailsService.saveListingsToMail(listingsDb, playerMock, quantity) } just Runs
        every { logsService.buyItemLog(playerMock, listingsDb, quantity, money) } just Runs
        every { notificationService.sellerItemNotification(listingsDb, quantity, money) } just Runs
        every { playerMock.sendMessage(finalMessage) } just Runs
        every { listingsService.getPaginated(pagination = playerView) } returns playerView
        every { listingsInventoryService.initInventory(playerView, playerMock) } returns inventory
        every { playerMock.openInventory(inventory) } returns inventoryView

        // WHEN
        marketService.buyItem(playerMock, 0, quantity, false)

        // THEN
        verify(exactly = 1) {
            listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)
            economy.has(offlinePlayer, money)
            economy.withdrawPlayer(offlinePlayer, money)
            economy.depositPlayer(offlinePlayer, money)
            listingsRepository.delete(listings.id!!)
            mailsService.saveListingsToMail(listingsDb, playerMock, quantity)
            logsService.buyItemLog(playerMock, listingsDb, quantity, money)
            notificationService.sellerItemNotification(listingsDb, quantity, money)
            playerMock.sendMessage(finalMessage)
            listingsService.getPaginated(pagination = playerView)
            listingsInventoryService.initInventory(playerView, playerMock)
            playerMock.openInventory(inventory)
        }
    }
}
