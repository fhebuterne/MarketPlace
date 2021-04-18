package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryType.LISTINGS
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadeableString
import fr.fabienhebuterne.marketplace.utils.formatInterval
import fr.fabienhebuterne.marketplace.utils.parseMaterialConfig
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class ListingsInventoryService(
    private val instance: MarketPlace,
    private val listingsService: ListingsService,
    inventoryOpenedService: InventoryOpenedService
) :
    InventoryTypeService<Listings>(instance, listingsService, inventoryOpenedService, LISTINGS) {
    private val playersConfirmation: MutableMap<UUID, Paginated> = mutableMapOf()

    override fun initInventory(pagination: Pagination<Listings>, player: Player): Inventory {
        val inventory =
            instance.loader.server.createInventory(player, CommandListings.BIG_CHEST_SIZE, "MarketPlace - Achat")

        pagination.results.forEachIndexed { index, listings ->
            val itemStack = if (listings.sellerUuid == player.uniqueId) {
                setSellerBottomLore(listings.itemStack.clone(), listings)
            } else {
                setBaseBottomLore(listings.itemStack.clone(), listings, player)
            }

            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory, pagination)

        return inventory
    }

    private fun setSellerBottomLore(itemStack: ItemStack, paginated: Listings): ItemStack {
        val itemMeta = itemStack.itemMeta
        var loreItem: MutableList<String> = mutableListOf()

        if (itemMeta?.hasLore() == true) {
            loreItem = itemMeta.lore?.toMutableList() ?: mutableListOf()
        }

        loreItem.addAll(instance.tl.listingItemBottomLoreSeller.toMutableList())
        loreItem.replaceAll {
            it.replace(ConfigPlaceholder.PRICE.placeholder, paginated.price.toString())
                .replace(ConfigPlaceholder.QUANTITY.placeholder, paginated.quantity.toString())
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { interval ->
                loreItem.replaceAll { it.replace(ConfigPlaceholder.EXPIRATION.placeholder, interval) }
            }
        } ?: loreItem.removeIf { it.contains(ConfigPlaceholder.EXPIRATION_BOOLEAN.placeholder) }

        loreItem.replaceAll {
            it.replace(ConfigPlaceholder.EXPIRATION_BOOLEAN.placeholder, "")
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

        loreItem.addAll(instance.tl.listingItemBottomLorePlayer.toMutableList())
        loreItem.replaceAll {
            it.replace(ConfigPlaceholder.SELLER_PSEUDO.placeholder, paginated.sellerPseudo)
                .replace(ConfigPlaceholder.PRICE.placeholder, convertDoubleToReadeableString(paginated.price))
                .replace(ConfigPlaceholder.QUANTITY.placeholder, paginated.quantity.toString())
        }

        if (paginated.quantity < 2) {
            loreItem.removeIf { it.contains(ConfigPlaceholder.MIDDLE_BOOLEAN.placeholder) }
        }

        if (paginated.quantity < 64) {
            loreItem.removeIf { it.contains(ConfigPlaceholder.RIGHT_BOOLEAN.placeholder) }
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { listings ->
                loreItem.replaceAll { it.replace(ConfigPlaceholder.EXPIRATION.placeholder, listings) }
            }
        } ?: loreItem.removeIf { it.contains(ConfigPlaceholder.EXPIRATION_BOOLEAN.placeholder) }

        loreItem.replaceAll {
            it.replace(ConfigPlaceholder.MIDDLE_BOOLEAN.placeholder, "")
                .replace(ConfigPlaceholder.RIGHT_BOOLEAN.placeholder, "")
                .replace(ConfigPlaceholder.EXPIRATION_BOOLEAN.placeholder, "")
        }

        if (player.hasPermission("marketplace.listings.other.remove")) {
            loreItem.addAll(instance.tl.listingItemBottomLorePlayerAdmin)
        }

        itemMeta?.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun confirmationAddNewItem(player: Player, listings: Listings): Inventory {
        playersConfirmation[player.uniqueId] = listings
        val inventory = Bukkit.createInventory(player, 9, "MarketPlace - Vente - Confirmation")

        val validItem = parseMaterialConfig(instance.conf.inventoryValidItem)
        val validItemMeta = validItem.itemMeta
        validItemMeta?.setDisplayName("§aValider")
        validItem.itemMeta = validItemMeta
        inventory.setItem(2, validItem)

        val cancelItem = parseMaterialConfig(instance.conf.inventoryCancelItem)
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
        player.sendMessage(instance.tl.cancelSelling)
        player.closeInventory()
    }

    fun openListingsInventory(player: Player) {
        val inventoryPaginated = listingsService.getPaginated(
            pagination = Pagination(
                currentPlayer = player.uniqueId,
                viewPlayer = player.uniqueId
            )
        )
        val mailsInventory = initInventory(inventoryPaginated, player)
        openInventory(player, mailsInventory)
    }
}
