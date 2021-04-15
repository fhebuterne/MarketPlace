package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.initItemStackMock
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadeableString
import io.mockk.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ListingsServiceTest : BaseTest() {

    private val listingsRepository: ListingsRepository = mockk()
    private val logsService: LogsService = mockk()
    private val clock = Clock.fixed(Instant.parse("2021-04-15T12:00:00.000Z"), ZoneId.systemDefault())

    private val listingsService = ListingsService(marketPlace, listingsRepository, logsService, clock)

    @Test
    fun `should create listings`() {
        // GIVEN
        val itemStack: ItemStack = initItemStackMock(Material.DIAMOND, 2)

        val listings = Listings(
            sellerUuid = fabienUuid,
            sellerPseudo = "Fabien91",
            itemStack = itemStack,
            quantity = 10,
            price = 100.0,
            world = "world",
            auditData = AuditData(createdAt = clock.millis()),
            version = 1343
        )

        val listingsCreatedMessage =
            translation.listingCreated.replace(ConfigPlaceholder.QUANTITY.placeholder, listings.quantity.toString())
                .replace(ConfigPlaceholder.ITEM_STACK.placeholder, listings.itemStack.type.toString())
                .replace(ConfigPlaceholder.UNIT_PRICE.placeholder, convertDoubleToReadeableString(listings.price))

        every { listingsService.create(listings) } returns listings
        every { logsService.saveListingsLog(playerMock, listings, listings.quantity, listings.price) } just Runs
        every { playerMock.sendMessage(listingsCreatedMessage) } just Runs
        val playerInventory: PlayerInventory = mockk()
        every { playerMock.inventory } returns playerInventory

        val slot = slot<ItemStack>()

        every { playerInventory.setItemInMainHand(capture(slot)) } just Runs

        // WHEN
        listingsService.create(playerMock, listings)

        // THEN
        expectThat(slot.captured)
            .get(ItemStack::getType)
            .isEqualTo(Material.AIR)

        verify(exactly = 1) {
            listingsService.create(listings)
            logsService.saveListingsLog(playerMock, listings, listings.quantity, listings.price)
            playerMock.sendMessage(listingsCreatedMessage)
            playerInventory.setItemInMainHand(any())
        }
    }

    @Test
    fun `should update listings`() {
        // GIVEN
        val itemStack: ItemStack = initItemStackMock(Material.DIAMOND, 2)

        val auditData = AuditData(createdAt = clock.millis())
        val listings = Listings(
            sellerUuid = fabienUuid,
            sellerPseudo = "Fabien91",
            itemStack = itemStack,
            quantity = 10,
            price = 100.0,
            world = "world",
            auditData = auditData,
            version = 1343
        )

        val updatedListings = listings.copy(
            quantity = 30,
            auditData = auditData.copy(
                updatedAt = clock.millis(),
                expiredAt = clock.millis() + (marketPlace.conf.expiration.listingsToMails * 1000)
            )
        )

        val listingsUpdatedMessage =
            translation.listingUpdated.replace(
                ConfigPlaceholder.ADDED_QUANTITY.placeholder,
                "20"
            )
                .replace(ConfigPlaceholder.ITEM_STACK.placeholder, itemStack.type.toString())
                .replace(ConfigPlaceholder.TOTAL_QUANTITY.placeholder, updatedListings.quantity.toString())

        every { listingsService.update(updatedListings) } returns listings
        every { logsService.saveListingsLog(playerMock, updatedListings, 20, listings.price) } just Runs
        every { playerMock.sendMessage(listingsUpdatedMessage) } just Runs
        val playerInventory: PlayerInventory = mockk()
        every { playerMock.inventory } returns playerInventory
        val slot = slot<ItemStack>()
        every { playerInventory.setItemInMainHand(capture(slot)) } just Runs

        // WHEN
        listingsService.updateListings(listings, 20, playerMock)

        // THEN
        expectThat(slot.captured)
            .get(ItemStack::getType)
            .isEqualTo(Material.AIR)

        verify(exactly = 1) {
            listingsService.update(updatedListings)
            logsService.saveListingsLog(playerMock, updatedListings, 20, listings.price)
            playerMock.sendMessage(listingsUpdatedMessage)
            playerInventory.setItemInMainHand(any())
        }
    }

}