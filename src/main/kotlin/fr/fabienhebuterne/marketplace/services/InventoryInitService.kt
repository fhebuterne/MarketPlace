package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.Listings
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.text.MessageFormat
import java.util.*

class InventoryInitService {

    val playersConfirmation: MutableMap<UUID, Listings> = mutableMapOf()

    fun listingsInventory(instance: JavaPlugin, pagination: Pagination<Listings>, player: Player): Inventory {
        val inventory = instance.server.createInventory(player, CommandListings.BIG_CHEST_SIZE, "MarketPlace - Achat")

        pagination.results.forEachIndexed { index, listings ->
            val itemStack = setBottomLore(listings.itemStack, listings)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory)

        return inventory
    }

    fun setBottomInventoryLine(inventory: Inventory) {
        val grayStainedGlassPane = ItemStack(Material.STAINED_GLASS_PANE, 1, 7)

        val filterItem = ItemStack(Material.REDSTONE_COMPARATOR)
        val filterItemMeta = filterItem.itemMeta
        filterItemMeta.displayName = "§aFiltrer par : ???"
        filterItem.itemMeta = filterItemMeta

        inventory.setItem(47, grayStainedGlassPane)
        inventory.setItem(48, grayStainedGlassPane)
        inventory.setItem(49, filterItem)
        inventory.setItem(50, grayStainedGlassPane)
        inventory.setItem(51, grayStainedGlassPane)

        InventoryLoreEnum.values().forEach {
            inventory.setItem(it.rawSlot, it.itemStack)
        }
    }

    // TODO : Add step lore to confirm
    fun setBottomLore(itemStack: ItemStack, listings: Listings): ItemStack {
        val itemMeta = itemStack.itemMeta
        val loreItem = mutableListOf<String>()
        loreItem.add("")
        loreItem.add(MessageFormat.format("§6Seller: §e{0}", listings.sellerPseudo))
        loreItem.add(MessageFormat.format("§6Price per item: §e{0}", listings.price))
        loreItem.add(MessageFormat.format("§6Total available: §e{0} items", listings.quantity))
        loreItem.add("")
        loreItem.add("§6► Left click to buy 1 item")

        if (listings.quantity >= 64) {
            loreItem.add("§6► Right click to buy 64 items")
        }

        loreItem.add("§6► Shift + Click to cancel")
        loreItem.add("")
        itemMeta.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun confirmationAddNewItem(player: Player, listings: Listings): Inventory {
        playersConfirmation[player.uniqueId] = listings
        val inventory = Bukkit.createInventory(player, 9, "MarketPlace - Vente - Confirmation")

        val validItem = ItemStack(Material.STAINED_GLASS_PANE, 1, 5)
        val validItemMeta = validItem.itemMeta
        validItemMeta.displayName = "§aValider"
        validItem.itemMeta = validItemMeta
        inventory.setItem(2, validItem)

        val cancelItem = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
        val cancelItemMeta = cancelItem.itemMeta
        cancelItemMeta.displayName = "§cAnnuler"
        cancelItem.itemMeta = cancelItemMeta
        inventory.setItem(6, cancelItem)
        return inventory
    }


}