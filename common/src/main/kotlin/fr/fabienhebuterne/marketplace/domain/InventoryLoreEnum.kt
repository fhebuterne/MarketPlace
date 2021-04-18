package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.domain.config.InventoryEnum
import fr.fabienhebuterne.marketplace.domain.config.Item
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

const val PREVIOUS_PAGE_TEXTURE =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19=="
const val NEXT_PAGE_TEXTURE =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ=="
const val SEARCH_TEXTURE =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMzNWU4Njg0YzdmNzc2YmVmZWRjNDMxOWQwODE0OGM1NGJlYTM5MzIxZTFiZDVkZWY3YTU1Yjg5ZmRhYTA5OSJ9fX0="
const val MAIL_TEXTURE =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJmM2ZjZGNjZmZkOTYzZTQzMzQ4MTgxMDhlMWU5YWUzYTgwNTY2ZDBkM2QyZDRhYjMwNTFhMmNkODExMzQ4YyJ9fX0="
const val LISTING_TEXTURE =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmNGFhY2ZjNjI3ZWU2NGEzMjU3YWQ1Mjc4MzU1NWYyYjM1OGNiN2NjY2I4NjQwNDA5OTc0MDNhNWIxYmQ1MiJ9fX0="

private val defaultItemStack = ItemStack(Material.DIRT)
private val defaultItem = Item("")

enum class InventoryLoreEnum(
    val rawSlot: Int,
    var itemStack: ItemStack = defaultItemStack,
    var item: Item = defaultItem,
    val inventoryType: InventoryType? = null
) {
    SEARCH(45),
    LISTING(46, inventoryType = InventoryType.MAILS),
    MAIL(46, inventoryType = InventoryType.LISTINGS),
    FILTER(49),
    PREVIOUS_PAGE(52),
    NEXT_PAGE(53);

    init {
        val itemMeta = itemStack.itemMeta
        itemMeta?.setDisplayName(item.displayName)
        itemMeta?.lore = item.lore
        itemStack.itemMeta = itemMeta
    }
}

fun loadMaterialFilterConfig(filter: String) {
    InventoryLoreEnum.FILTER.itemStack = ItemStack(Material.valueOf(filter))
}

fun loadInventoryLoreTranslation(inventoryEnum: InventoryEnum) {
    InventoryLoreEnum.SEARCH.item = inventoryEnum.search
    InventoryLoreEnum.LISTING.item = inventoryEnum.listings
    InventoryLoreEnum.MAIL.item = inventoryEnum.mails
    InventoryLoreEnum.PREVIOUS_PAGE.item = inventoryEnum.previousPage
    InventoryLoreEnum.NEXT_PAGE.item = inventoryEnum.nextPage
}

fun loadSkull(itemStackReflection: IItemStackReflection) {
    InventoryLoreEnum.SEARCH.itemStack = itemStackReflection.getSkull(SEARCH_TEXTURE)
    InventoryLoreEnum.LISTING.itemStack = itemStackReflection.getSkull(LISTING_TEXTURE)
    InventoryLoreEnum.MAIL.itemStack = itemStackReflection.getSkull(MAIL_TEXTURE)
    InventoryLoreEnum.PREVIOUS_PAGE.itemStack = itemStackReflection.getSkull(PREVIOUS_PAGE_TEXTURE)
    InventoryLoreEnum.NEXT_PAGE.itemStack = itemStackReflection.getSkull(NEXT_PAGE_TEXTURE)
}
