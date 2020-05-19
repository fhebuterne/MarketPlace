package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.nms.ItemStackReflection
import org.bukkit.inventory.ItemStack

const val PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19=="
const val NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ=="
const val SEARCH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMzNWU4Njg0YzdmNzc2YmVmZWRjNDMxOWQwODE0OGM1NGJlYTM5MzIxZTFiZDVkZWY3YTU1Yjg5ZmRhYTA5OSJ9fX0="
const val MAIL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJmM2ZjZGNjZmZkOTYzZTQzMzQ4MTgxMDhlMWU5YWUzYTgwNTY2ZDBkM2QyZDRhYjMwNTFhMmNkODExMzQ4YyJ9fX0="

enum class InventoryLoreEnum(
        val rawSlot: Int,
        val itemStack: ItemStack,
        displayName: String,
        textureBase64: String,
        var lore: MutableList<String> = mutableListOf()
) {
    SEARCH(45, ItemStackReflection.getSkull(SEARCH_TEXTURE), "§cRecherche...", SEARCH_TEXTURE),
    MAIL(46, ItemStackReflection.getSkull(MAIL_TEXTURE), "§6Boite de réception", MAIL_TEXTURE),
    PREVIOUS_PAGE(52, ItemStackReflection.getSkull(PREVIOUS_PAGE_TEXTURE), "§cPage précédente", PREVIOUS_PAGE_TEXTURE),
    NEXT_PAGE(53, ItemStackReflection.getSkull(NEXT_PAGE_TEXTURE), "§cPage suivante", NEXT_PAGE_TEXTURE);

    init {
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName = displayName
        itemMeta.lore = lore
        itemStack.itemMeta = itemMeta
    }
}