package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.Listings
import fr.fabienhebuterne.marketplace.nms.ItemStackReflection.getSkull
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.text.MessageFormat

class InventoryInitService {

    public fun setBottomInventoryLine(inventory: Inventory) {
        val grayStainedGlassPane = ItemStack(Material.STAINED_GLASS_PANE, 1, 7)

        val searchItem = getSkull(CommandListings.SEARCH_TEXTURE)
        val searchItemMeta = searchItem.itemMeta
        searchItemMeta.displayName = "§cRecherche..."
        searchItem.itemMeta = searchItemMeta

        val mailItem = getSkull(CommandListings.MAIL_TEXTURE)
        val mailItemMeta = mailItem.itemMeta
        mailItemMeta.displayName = "§6Boite de réception"
        mailItem.itemMeta = mailItemMeta

        val filterItem = ItemStack(Material.REDSTONE_COMPARATOR)
        val filterItemMeta = filterItem.itemMeta
        filterItemMeta.displayName = "§aFiltrer par : ???"
        filterItem.itemMeta = filterItemMeta

        val previousPageItem = getSkull(CommandListings.PREVIOUS_PAGE_TEXTURE)
        val previousPageItemMeta = previousPageItem.itemMeta
        previousPageItemMeta.displayName = "§cPage précédente"
        previousPageItem.itemMeta = previousPageItemMeta


        val nextPageItem = getSkull(CommandListings.NEXT_PAGE_TEXTURE)
        val nextPageItemMeta = nextPageItem.itemMeta
        nextPageItemMeta.displayName = "§cPage suivante"
        nextPageItem.itemMeta = nextPageItemMeta

        inventory.setItem(45, searchItem)
        inventory.setItem(46, mailItem)
        inventory.setItem(47, grayStainedGlassPane)
        inventory.setItem(48, grayStainedGlassPane)
        inventory.setItem(49, filterItem)
        inventory.setItem(50, grayStainedGlassPane)
        inventory.setItem(51, grayStainedGlassPane)
        inventory.setItem(52, previousPageItem)
        inventory.setItem(53, nextPageItem)
    }

    // TODO : Add step lore to confirm
    public fun setBottomLore(itemStack: ItemStack, listings: Listings): ItemStack {
        val itemMeta = itemStack.itemMeta
        val loreItem = mutableListOf<String>()
        loreItem.add("")
        loreItem.add(MessageFormat.format("§6Seller: §e{0}", listings.sellerPseudo))
        loreItem.add(MessageFormat.format("§6Stack per click: §e{0}", listings.quantity))
        loreItem.add(MessageFormat.format("§6Total available: §e{0} items", listings.amount * listings.quantity))
        loreItem.add("")
        loreItem.add(MessageFormat.format("§6► Left click to buy {0} items", listings.quantity))
        loreItem.add(MessageFormat.format("§6► Shift + Click to cancel"))
        loreItem.add("")
        itemMeta.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

}