package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.services.inventory.InventoryOpenedService
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.kodein.di.DI
import org.kodein.di.instance

class InventoryCloseEventListener(kodein: DI) : BaseListener<InventoryCloseEvent>() {

    private val inventoryOpenedService: InventoryOpenedService by kodein.instance<InventoryOpenedService>()

    override fun execute(event: InventoryCloseEvent) {
        val player: Player = event.view.player as Player
        inventoryOpenedService.inventoryOpened.remove(player.uniqueId)
    }

}
