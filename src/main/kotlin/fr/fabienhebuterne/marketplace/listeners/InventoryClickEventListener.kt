package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class InventoryClickEventListener(private val marketPlace: MarketPlace, kodein: Kodein) : Listener {

    private val marketService: MarketService by kodein.instance<MarketService>()
    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()
    private val mailsService: MailsService by kodein.instance<MailsService>()
    private val mailsInventoryService: MailsInventoryService by kodein.instance<MailsInventoryService>()

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val player: Player = event.view.player as Player

        if (event.view.title.contains("MarketPlace")) {
            event.isCancelled = true
        }

        if (event.view.title == "MarketPlace - Achat") {
            marketService.clickOnListingsInventory(event, player)
            clickOnBottomLineListings(event, player)
        }

        if (event.view.title == "MarketPlace - Mails") {
            marketService.clickOnMailsInventory(event, player)
            clickOnBottomLineMails(event, player)
        }

        if (event.view.title == "MarketPlace - Vente - Confirmation") {
            listingsInventoryService.clickOnAddNewItemConfirmation(event, player)
        }

    }

    // TODO : Refactoring common code between listings and mails
    private fun clickOnBottomLineListings(event: InventoryClickEvent, player: Player) {
        listingsInventoryService.clickOnSwitchPage(marketPlace, event, player)

        if (event.rawSlot == InventoryLoreEnum.MAIL.rawSlot) {
            val inventoryPaginated = mailsService.getPaginated(player.uniqueId, 1)
            val mailsInventory = mailsInventoryService.initInventory(marketPlace, inventoryPaginated, player)
            player.openInventory(mailsInventory)
        }
    }

    private fun clickOnBottomLineMails(event: InventoryClickEvent, player: Player) {
        mailsInventoryService.clickOnSwitchPage(marketPlace, event, player)

        if (event.rawSlot == InventoryLoreEnum.LISTING.rawSlot) {
            val inventoryPaginated = listingsService.getPaginated(player.uniqueId, 1)
            val listingsInventory = listingsInventoryService.initInventory(marketPlace, inventoryPaginated, player)
            player.openInventory(listingsInventory)
        }
    }

}