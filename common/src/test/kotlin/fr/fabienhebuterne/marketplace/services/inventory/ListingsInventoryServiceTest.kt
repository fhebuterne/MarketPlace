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
import strikt.api.expect
import strikt.assertions.isEqualTo
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
    fun `should init listings inventory with player is not seller with base bottom lore without admin permission`() {
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

        this.`should init listings inventory without admin permission`(ergailUuid, "Ergail", fabienUuid, loreExcepted)
    }

    @Test
    fun `should init listings inventory when player is seller without admin permission`() {
        val loreExcepted = listOf(
            "",
            "§6Prix unité: §e10$",
            "§6Total disponible: §e10 article(s)",
            "",
            "§c► Vous ne pouvez pas acheter vos articles",
            "§6► Shift + Clique gauche pour retirer vos articles",
            ""
        )

        this.`should init listings inventory without admin permission`(fabienUuid, "Fabien91", fabienUuid, loreExcepted)
    }

    private fun `should init listings inventory without admin permission`(
        sellerUuid: UUID,
        sellerPseudo: String,
        currentPlayer: UUID,
        loreExcepted: List<String>
    ) {
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
                    sellerPseudo = sellerPseudo,
                    sellerUuid = sellerUuid,
                    world = "world",
                    version = 1343
                )
            ),
            currentPlayer = currentPlayer,
            viewPlayer = currentPlayer
        )

        val inventory: Inventory = mockk()
        every {
            serverMock.createInventory(
                playerMock,
                CommandListings.BIG_CHEST_SIZE,
                translation.inventoryType[InventoryType.LISTINGS] ?: ""
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
                playerMock,
                CommandListings.BIG_CHEST_SIZE,
                translation.inventoryType[InventoryType.LISTINGS] ?: ""
            )
            itemMeta.lore = loreExcepted
            inventory.setItem(0, itemStack)
        }

        verify(exactly = 9) {
            inventory.setItem(more(44), any())
        }
    }

    @Test
    fun `should init confirmation inventory`() {
        // GIVEN
        val itemMeta: ItemMeta = mockk()
        every { itemFactory.getItemMeta(any()) } returns itemMeta
        every { itemMeta.setDisplayName(any()) } just Runs
        every { itemMeta.lore = any() } just Runs
        every { itemFactory.isApplicable(any(), any<Material>()) } returns true
        every { itemFactory.asMetaFor(any(), any<Material>()) } returns itemMeta
        every { itemMeta.clone() } returns itemMeta
        every { itemFactory.equals(any(), any()) } returns false

        val inventory: Inventory = mockk()
        every {
            serverMock.createInventory(
                playerMock,
                9,
                translation.inventoryType[InventoryType.SELL_CONFIRMATION] ?: ""
            )
        } returns inventory

        val itemStack: ItemStack = mockk()
        val listings = Listings(
            id = UUID.randomUUID(),
            auditData = AuditData(createdAt = System.currentTimeMillis()),
            itemStack = itemStack,
            quantity = 10,
            price = 10.0,
            sellerPseudo = "Fabien91",
            sellerUuid = fabienUuid,
            world = "world",
            version = 1343
        )

        val validItemStack: ItemStack = initItemStackMock(Material.STAINED_GLASS_PANE)
        every { inventory.setItem(2, any()) } just Runs
        every { inventory.getItem(2) } returns validItemStack

        val cancelItemStack: ItemStack = initItemStackMock(Material.STAINED_GLASS_PANE)
        every { inventory.setItem(6, any()) } just Runs
        every { inventory.getItem(6) } returns cancelItemStack

        // WHEN
        val inventoryReturn = listingsInventoryService.confirmationAddNewItem(playerMock, listings)

        // THEN
        expect {
            that(inventoryReturn.getItem(2)).get {
                this.type
            }.isEqualTo(Material.STAINED_GLASS_PANE)
            that(inventoryReturn.getItem(6)).get {
                this.type
            }.isEqualTo(Material.STAINED_GLASS_PANE)
        }

    }

}
