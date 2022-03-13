package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.domain.config.InventoryEnum
import fr.fabienhebuterne.marketplace.domain.config.Item
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

const val PREVIOUS_PAGE_TEXTURE_URL = "https://textures.minecraft.net/texture/cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5"
const val NEXT_PAGE_TEXTURE_URL = "https://textures.minecraft.net/texture/956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311"
const val SEARCH_TEXTURE_URL = "https://textures.minecraft.net/texture/fc35e8684c7f776befedc4319d08148c54bea39321e1bd5def7a55b89fdaa099"
const val MAIL_TEXTURE_URL = "https://textures.minecraft.net/texture/6bf3fcdccffd963e4334818108e1e9ae3a80566d0d3d2d4ab3051a2cd811348c"
const val LISTING_TEXTURE_URL = "https://textures.minecraft.net/texture/e9f4aacfc627ee64a3257ad52783555f2b358cb7cccb864040997403a5b1bd52"

private val defaultItemStack = ItemStack(Material.DIRT)
private val defaultItem = Item("missing translation")

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
    InventoryLoreEnum.SEARCH.itemStack = itemStackReflection.getSkull(SEARCH_TEXTURE_URL)
    InventoryLoreEnum.LISTING.itemStack = itemStackReflection.getSkull(LISTING_TEXTURE_URL)
    InventoryLoreEnum.MAIL.itemStack = itemStackReflection.getSkull(MAIL_TEXTURE_URL)
    InventoryLoreEnum.PREVIOUS_PAGE.itemStack = itemStackReflection.getSkull(PREVIOUS_PAGE_TEXTURE_URL)
    InventoryLoreEnum.NEXT_PAGE.itemStack = itemStackReflection.getSkull(NEXT_PAGE_TEXTURE_URL)
}
