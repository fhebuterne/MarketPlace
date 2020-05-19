package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.services.InventoryInitService
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class InventoryClickEventListener(private val marketPlace: MarketPlace, kodein: Kodein) : Listener {

    private val marketService: MarketService by kodein.instance<MarketService>()
    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val inventoryInitService: InventoryInitService by kodein.instance<InventoryInitService>()
    private val listingsRepository: ListingsRepository by kodein.instance<ListingsRepository>()

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val player: Player = event.view.player as Player

        if (event.view.title.contains("MarketPlace")) {
            event.isCancelled = true
        }

        if (event.view.title == "MarketPlace - Achat") {
            clickOnListingsInventory(event, player)
        }

        if (event.view.title == "MarketPlace - Vente - Confirmation") {
            clickOnAddNewItemConfirmation(event, player)
        }

    }

    private fun clickOnAddNewItemConfirmation(event: InventoryClickEvent, player: Player) {
        if (event.slotType != InventoryType.SlotType.CONTAINER) {
            return
        }

        if (event.rawSlot == 2) {
            val paginated = inventoryInitService.playersConfirmation[player.uniqueId]
            if (paginated != null && paginated is Listings) {
                listingsRepository.create(paginated)
                inventoryInitService.playersConfirmation.remove(player.uniqueId)
                player.sendMessage("created item after confirmation ok")
                player.itemInHand = ItemStack(Material.AIR)
            }
            player.closeInventory()
        }

        if (event.rawSlot == 6) {
            val listings = inventoryInitService.playersConfirmation[player.uniqueId]
            if (listings != null) {
                inventoryInitService.playersConfirmation.remove(player.uniqueId)
            }
            player.sendMessage("cancelled sell item")
            player.closeInventory()
        }
    }

    private fun clickOnListingsInventory(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.PREVIOUS_PAGE.rawSlot) {
            val nextPageListings = listingsService.previousPage(player.uniqueId)
            val listingsInventory = inventoryInitService.listingsInventory(marketPlace, nextPageListings, player)
            player.openInventory(listingsInventory)
        }

        if (event.rawSlot == InventoryLoreEnum.NEXT_PAGE.rawSlot) {
            val nextPageListings = listingsService.nextPage(player.uniqueId)
            val listingsInventory = inventoryInitService.listingsInventory(marketPlace, nextPageListings, player)
            player.openInventory(listingsInventory)
        }

        if (event.rawSlot in 0..44) {
            if (event.currentItem.type == Material.AIR) {
                return
            }

            if (event.isLeftClick) {
                marketService.buyItem(player, event.rawSlot, 1)
            }

            if (event.isRightClick) {
                marketService.buyItem(player, event.rawSlot, 64)
            }

            if (event.click == ClickType.MIDDLE) {
                println("middle click on " + (listingsService.playersView[player.uniqueId]?.results?.get(event.rawSlot)
                        ?: "null"))
            }
        }
    }

}