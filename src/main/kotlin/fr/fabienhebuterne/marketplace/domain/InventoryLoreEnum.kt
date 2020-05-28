package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.nms.ItemStackReflection
import fr.fabienhebuterne.marketplace.tl
import org.bukkit.inventory.ItemStack

const val PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19=="
const val NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ=="
const val SEARCH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMzNWU4Njg0YzdmNzc2YmVmZWRjNDMxOWQwODE0OGM1NGJlYTM5MzIxZTFiZDVkZWY3YTU1Yjg5ZmRhYTA5OSJ9fX0="
const val MAIL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJmM2ZjZGNjZmZkOTYzZTQzMzQ4MTgxMDhlMWU5YWUzYTgwNTY2ZDBkM2QyZDRhYjMwNTFhMmNkODExMzQ4YyJ9fX0="
const val LISTING_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmNGFhY2ZjNjI3ZWU2NGEzMjU3YWQ1Mjc4MzU1NWYyYjM1OGNiN2NjY2I4NjQwNDA5OTc0MDNhNWIxYmQ1MiJ9fX0="

enum class InventoryLoreEnum(
        val rawSlot: Int,
        val itemStack: ItemStack,
        var displayName: String,
        var lore: List<String> = listOf(),
        val inventoryType: InventoryType? = null
) {
    SEARCH(45, ItemStackReflection.getSkull(SEARCH_TEXTURE), tl.inventoryEnum.search.displayName, tl.inventoryEnum.search.lore),
    LISTING(46, ItemStackReflection.getSkull(LISTING_TEXTURE), tl.inventoryEnum.listings.displayName, tl.inventoryEnum.listings.lore, inventoryType = InventoryType.MAILS),
    MAIL(46, ItemStackReflection.getSkull(MAIL_TEXTURE), tl.inventoryEnum.mails.displayName, tl.inventoryEnum.mails.lore, inventoryType = InventoryType.LISTINGS),
    PREVIOUS_PAGE(52, ItemStackReflection.getSkull(PREVIOUS_PAGE_TEXTURE), tl.inventoryEnum.previousPage.displayName, tl.inventoryEnum.previousPage.lore),
    NEXT_PAGE(53, ItemStackReflection.getSkull(NEXT_PAGE_TEXTURE), tl.inventoryEnum.nextPage.displayName, tl.inventoryEnum.nextPage.lore);

    init {
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName = displayName
        itemMeta.lore = lore
        itemStack.itemMeta = itemMeta
    }
}

enum class InventoryType {
    MAILS,
    LISTINGS
}

fun reloadTranslation() {
    InventoryLoreEnum.SEARCH.displayName = tl.inventoryEnum.search.displayName
    InventoryLoreEnum.SEARCH.lore = tl.inventoryEnum.search.lore
    InventoryLoreEnum.LISTING.displayName = tl.inventoryEnum.listings.displayName
    InventoryLoreEnum.LISTING.lore = tl.inventoryEnum.listings.lore
    InventoryLoreEnum.MAIL.displayName = tl.inventoryEnum.mails.displayName
    InventoryLoreEnum.MAIL.lore = tl.inventoryEnum.mails.lore
    InventoryLoreEnum.PREVIOUS_PAGE.displayName = tl.inventoryEnum.previousPage.displayName
    InventoryLoreEnum.PREVIOUS_PAGE.lore = tl.inventoryEnum.previousPage.lore
    InventoryLoreEnum.NEXT_PAGE.displayName = tl.inventoryEnum.nextPage.displayName
    InventoryLoreEnum.NEXT_PAGE.lore = tl.inventoryEnum.nextPage.lore
}