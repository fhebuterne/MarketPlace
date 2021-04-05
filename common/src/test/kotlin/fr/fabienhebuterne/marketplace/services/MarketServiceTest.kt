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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFactory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import strikt.api.expectCatching
import strikt.assertions.isA
import strikt.assertions.isFailure
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarketServiceTest : BaseTest() {

    private val listingsService: ListingsService = mockk()
    private val listingsRepository: ListingsRepository = mockk()
    private val listingsInventoryService: ListingsInventoryService = mockk()
    private val mailsService: MailsService = mockk()
    private val mailsRepository: MailsRepository = mockk()
    private val mailsInventoryService: MailsInventoryService = mockk()
    private val logsService: LogsService = mockk()
    private var playerMock: Player = mockk()

    private val marketService: MarketService = MarketService(
        marketPlace,
        listingsService,
        listingsRepository,
        listingsInventoryService,
        mailsService,
        mailsRepository,
        mailsInventoryService,
        logsService
    )

    private fun initPlayerView(quantity: Int = 31): MutableMap<UUID, Pagination<Listings>> {
        val itemStack: ItemStack = mockk()

        val listings = mutableMapOf(
            Pair(
                fabienUuid,
                Pagination(
                    results = listOf(
                        Listings(
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

        return listings
    }

    @BeforeAll
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
        val listings: Listings = playerView[fabienUuid]?.results?.get(0)
            ?: throw IllegalAccessException("Can't found listings")
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
        val listings: Listings = playerView[fabienUuid]?.results?.get(0)
            ?: throw IllegalAccessException("Can't found listings")
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
        val listings: Listings = playerView[fabienUuid]?.results?.get(0)
            ?: throw IllegalAccessException("Can't found listings")
        val quantity = 30
        every {
            listingsRepository.find(
                listings.sellerUuid,
                listings.itemStack,
                listings.price
            )
        } returns listings.copy(quantity = quantity)
        every {
            marketPlace.getEconomy().has(
                Bukkit.getOfflinePlayer(fabienUuid),
                listings.price * quantity
            )
        } returns false
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
}
