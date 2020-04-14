package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickEventListener(val marketPlace: MarketPlace, val listingsRepository: ListingsRepository) : Listener {

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        if (event.view.title != "MarketPlace - Achat") {
            return
        }

        event.isCancelled = true

    }

}