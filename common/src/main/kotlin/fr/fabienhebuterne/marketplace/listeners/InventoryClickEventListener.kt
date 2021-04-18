package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.kodein.di.DI
import org.kodein.di.instance

class InventoryClickEventListener(private val marketPlace: MarketPlace, kodein: DI) :
    BaseListener<InventoryClickEvent>() {

    private val marketService: MarketService by kodein.instance<MarketService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()
    private val mailsInventoryService: MailsInventoryService by kodein.instance<MailsInventoryService>()

    @EventHandler
    override fun execute(event: InventoryClickEvent) {
        val player: Player = event.view.player as Player

        // TODO : Find better solution to check inventory (not using title) maybe extend Inventory class ?
        // keep reference from InventoryView after openInventory (hashCode())
        if (event.view.title.contains("MarketPlace")) {
            event.isCancelled = true
        }

        if (event.view.title == "MarketPlace - Achat") {
            marketService.clickOnListingsInventory(event, player)
            listingsInventoryService.clickOnBottomLineListings(event, player)
        }

        if (event.view.title.contains("MarketPlace - Mails")) {
            marketService.clickOnMailsInventory(event, player)
            mailsInventoryService.clickOnBottomLineMails(event, player)
        }

        if (event.view.title == "MarketPlace - Vente - Confirmation") {
            listingsInventoryService.clickOnAddNewItemConfirmation(event, player)
        }
    }

}
