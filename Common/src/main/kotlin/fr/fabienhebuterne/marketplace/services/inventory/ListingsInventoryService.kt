package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.conf
import fr.fabienhebuterne.marketplace.domain.InventoryType.LISTINGS
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.tl
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadeableString
import fr.fabienhebuterne.marketplace.utils.formatInterval
import fr.fabienhebuterne.marketplace.utils.parseMaterialConfig
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ListingsInventoryService(private val listingsService: ListingsService) :
    InventoryTypeService<Listings>(listingsService) {
    private val playersConfirmation: MutableMap<UUID, Paginated> = mutableMapOf()

    override fun initInventory(instance: JavaPlugin, pagination: Pagination<Listings>, player: Player): Inventory {
        val inventory = instance.server.createInventory(player, CommandListings.BIG_CHEST_SIZE, "MarketPlace - Achat")

        pagination.results.forEachIndexed { index, listings ->
            val itemStack = if (listings.sellerUuid == player.uniqueId) {
                setSellerBottomLore(listings.itemStack.clone(), listings)
            } else {
                setBaseBottomLore(listings.itemStack.clone(), listings, player)
            }

            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(instance, inventory, pagination)

        return inventory
    }

    private fun setSellerBottomLore(itemStack: ItemStack, paginated: Listings): ItemStack {
        val itemMeta = itemStack.itemMeta
        var loreItem: MutableList<String> = mutableListOf()

        if (itemMeta?.hasLore() == true) {
            loreItem = itemMeta.lore?.toMutableList() ?: mutableListOf()
        }

        loreItem.addAll(tl.listingItemBottomLoreSeller.toMutableList())
        loreItem.replaceAll {
            it.replace("{{price}}", paginated.price.toString())
                .replace("{{quantity}}", paginated.quantity.toString())
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { interval ->
                loreItem.replaceAll { it.replace("{{expiration}}", interval) }
            }
        } ?: loreItem.removeIf { it.contains("%expiration%") }

        loreItem.replaceAll {
            it.replace("%expiration%", "")
        }

        itemMeta?.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    override fun setBaseBottomLore(itemStack: ItemStack, paginated: Listings, player: Player): ItemStack {
        val itemMeta = itemStack.itemMeta
        var loreItem: MutableList<String> = mutableListOf()

        if (itemMeta?.hasLore() == true) {
            loreItem = itemMeta.lore?.toMutableList() ?: mutableListOf()
        }

        loreItem.addAll(tl.listingItemBottomLorePlayer.toMutableList())
        loreItem.replaceAll {
            it.replace("{{sellerPseudo}}", paginated.sellerPseudo)
                .replace("{{price}}", convertDoubleToReadeableString(paginated.price))
                .replace("{{quantity}}", paginated.quantity.toString())
        }

        if (paginated.quantity < 2) {
            loreItem.removeIf { it.contains("%middle%") }
        }

        if (paginated.quantity < 64) {
            loreItem.removeIf { it.contains("%right%") }
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { listings ->
                loreItem.replaceAll { it.replace("{{expiration}}", listings) }
            }
        } ?: loreItem.removeIf { it.contains("%expiration%") }

        loreItem.replaceAll {
            it.replace("%middle%", "")
                .replace("%right%", "")
                .replace("%expiration%", "")
        }

        if (player.hasPermission("marketplace.listings.other.remove")) {
            loreItem.addAll(tl.listingItemBottomLorePlayerAdmin)
        }

        itemMeta?.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun confirmationAddNewItem(player: Player, listings: Listings): Inventory {
        playersConfirmation[player.uniqueId] = listings
        val inventory = Bukkit.createInventory(player, 9, "MarketPlace - Vente - Confirmation")

        val validItem = parseMaterialConfig(conf.inventoryValidItem)
        val validItemMeta = validItem.itemMeta
        validItemMeta?.setDisplayName("§aValider")
        validItem.itemMeta = validItemMeta
        inventory.setItem(2, validItem)

        val cancelItem = parseMaterialConfig(conf.inventoryCancelItem)
        val cancelItemMeta = cancelItem.itemMeta
        cancelItemMeta?.setDisplayName("§cAnnuler")
        cancelItem.itemMeta = cancelItemMeta
        inventory.setItem(6, cancelItem)
        return inventory
    }

    fun clickOnAddNewItemConfirmation(event: InventoryClickEvent, player: Player) {
        if (event.slotType != InventoryType.SlotType.CONTAINER) {
            return
        }

        if (event.rawSlot == 2) {
            itemSell(player)
        }

        if (event.rawSlot == 6) {
            cancelSell(player)
        }
    }

    private fun itemSell(player: Player) {
        val paginated = playersConfirmation[player.uniqueId]
        if (paginated != null && paginated is Listings) {
            listingsService.create(player, paginated)
            playersConfirmation.remove(player.uniqueId)
        }
        player.closeInventory()
    }

    private fun cancelSell(player: Player) {
        val listings = playersConfirmation[player.uniqueId]
        if (listings != null) {
            playersConfirmation.remove(player.uniqueId)
        }
        player.sendMessage(tl.cancelSelling)
        player.closeInventory()
    }

    private fun setBottomInventoryLine(
        instance: JavaPlugin,
        inventory: Inventory,
        pagination: Pagination<out Paginated>
    ) {
        super.setBottomInventoryLine(instance, inventory, pagination, LISTINGS)
    }

    fun clickOnFilter(instance: JavaPlugin, event: InventoryClickEvent, player: Player) {
        super.clickOnFilter(instance, event, player, LISTINGS)
    }
}
