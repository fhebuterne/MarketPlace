package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.InventoryOpenedService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.kodein.di.DI
import org.kodein.di.instance

class InventoryClickEventListener(kodein: DI) :
    BaseListener<InventoryClickEvent>() {

    private val marketService: MarketService by kodein.instance<MarketService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()
    private val mailsInventoryService: MailsInventoryService by kodein.instance<MailsInventoryService>()
    private val inventoryOpenedService: InventoryOpenedService by kodein.instance<InventoryOpenedService>()

    override fun execute(event: InventoryClickEvent) {
        val player: Player = event.view.player as Player
        val inventoryOpened = inventoryOpenedService.inventoryOpened[player.uniqueId]

        if (inventoryOpened?.inventoryView == event.view) {
            event.isCancelled = true
        } else {
            return
        }

        if (inventoryOpened?.inventoryType == InventoryType.LISTINGS) {
            marketService.clickOnListingsInventory(event, player)
            clickOnBottomLineListings(event, player)
        }

        if (inventoryOpened?.inventoryType == InventoryType.MAILS) {
            marketService.clickOnMailsInventory(event, player)
            clickOnBottomLineMails(event, player)
        }

        if (inventoryOpened?.inventoryType == InventoryType.SELL_CONFIRMATION) {
            listingsInventoryService.clickOnAddNewItemConfirmation(event, player)
        }
    }

    private fun clickOnBottomLineListings(event: InventoryClickEvent, player: Player) {
        listingsInventoryService.clickOnSwitchPage(event, player)
        listingsInventoryService.clickOnSearch(event, player)
        listingsInventoryService.clickOnFilter(event, player)

        if (event.rawSlot == InventoryLoreEnum.MAIL.rawSlot) {
            mailsInventoryService.openMailsInventory(player)
        }
    }

    private fun clickOnBottomLineMails(event: InventoryClickEvent, player: Player) {
        mailsInventoryService.clickOnSwitchPage(event, player)
        mailsInventoryService.clickOnSearch(event, player)
        mailsInventoryService.clickOnFilter(event, player)

        if (event.rawSlot == InventoryLoreEnum.LISTING.rawSlot) {
            listingsInventoryService.openListingsInventory(player)
        }
    }

}
