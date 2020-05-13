package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.services.InventoryInitService
import fr.fabienhebuterne.marketplace.services.ListingsService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class InventoryClickEventListener(val marketPlace: MarketPlace, kodein: Kodein) : Listener {

    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val inventoryInitService: InventoryInitService by kodein.instance<InventoryInitService>()

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        if (event.view.title != "MarketPlace - Achat") {
            return
        }

        val player: Player = event.view.player as Player

        event.isCancelled = true

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

    }

}