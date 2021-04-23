package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.loadInventoryLoreTranslation
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.initItemStackMock
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import io.mockk.*
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.junit.jupiter.api.Test
import java.util.*

class ListingsInventoryServiceTest : BaseTest() {

    private val listingsService: ListingsService = mockk()
    private val inventoryOpenedService: InventoryOpenedService = mockk()

    private val listingsInventoryService: ListingsInventoryService = ListingsInventoryService(
        marketPlace,
        listingsService,
        inventoryOpenedService
    )

    @Test
    fun `should init listings inventory with base bottom lore without admin permission`() {
        // GIVEN
        val itemMeta: ItemMeta = mockk()
        every { itemFactory.getItemMeta(any()) } returns itemMeta
        every { itemMeta.setDisplayName(any()) } just Runs
        every { itemMeta.lore = any() } just Runs
        every { itemFactory.isApplicable(any(), any<Material>()) } returns true
        every { itemFactory.asMetaFor(any(), any<Material>()) } returns itemMeta
        every { itemMeta.clone() } returns itemMeta
        every { itemFactory.equals(any(), any()) } returns false

        loadInventoryLoreTranslation(translation.inventoryEnum)
        val itemStack = initItemStackMock(Material.DIAMOND, 1)
        val pagination = Pagination(
            results = listOf(
                Listings(
                    id = UUID.randomUUID(),
                    auditData = AuditData(createdAt = System.currentTimeMillis()),
                    itemStack = itemStack,
                    quantity = 10,
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
        val loreExcepted = listOf(
            "",
            "§6Vendeur: §eErgail",
            "§6Prix unité: §e10$",
            "§6Total disponible: §e10 article(s)",
            "",
            "§6► Clique gauche pour acheter 1 article",
            "§6► Clique molette pour acheter une quantité personnalisée d'articles",
            ""
        )

        val inventory: Inventory = mockk()
        every {
            serverMock.createInventory(
                playerMock, CommandListings.BIG_CHEST_SIZE,
                translation.inventoryType[InventoryType.LISTINGS]
            )
        } returns inventory
        every { itemStack.itemMeta } returns itemMeta
        every { itemMeta.hasLore() } returns false
        every { playerMock.hasPermission("marketplace.listings.other.remove") } returns false
        every {
            itemMeta.lore = loreExcepted
        } just Runs
        every { itemStack.setItemMeta(itemMeta) } returns true
        every { inventory.setItem(0, itemStack) } just Runs

        val slots = mutableListOf<ItemStack>()
        every { inventory.setItem(more(44), capture(slots)) } just Runs

        // WHEN
        listingsInventoryService.initInventory(pagination, playerMock)

        // THEN
        verify(exactly = 1) {
            serverMock.createInventory(
                playerMock, CommandListings.BIG_CHEST_SIZE,
                translation.inventoryType[InventoryType.LISTINGS]
            )
            itemMeta.lore = loreExcepted
            inventory.setItem(0, itemStack)
        }

        verify(exactly = 9) {
            inventory.setItem(more(44), any())
        }

    }

}
