package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.kodein.di.DI
import org.kodein.di.instance

class InventoryClickEventListener(private val marketPlace: MarketPlace, kodein: DI) : Listener {

    private val marketService: MarketService by kodein.instance<MarketService>()
    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()
    private val mailsService: MailsService by kodein.instance<MailsService>()
    private val mailsInventoryService: MailsInventoryService by kodein.instance<MailsInventoryService>()

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        try {
            execute(event)
        } catch (ignored: CustomException) {
            // We ignore CustomException because error msg send to player
            // and don't want to have stacktrace on console
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // TODO: Factory to put in common code for hidden exception
    private fun execute(event: InventoryClickEvent) {
        val player: Player = event.view.player as Player

        // TODO : Find better solution to check inventory (not using title) maybe extend Inventory class ?
        // keep reference from InventoryView after openInventory (hashCode())
        if (event.view.title.contains("MarketPlace")) {
            event.isCancelled = true
        }

        if (event.view.title == "MarketPlace - Achat") {
            marketService.clickOnListingsInventory(event, player)
            clickOnBottomLineListings(event, player)
        }

        if (event.view.title.contains("MarketPlace - Mails")) {
            marketService.clickOnMailsInventory(event, player)
            clickOnBottomLineMails(event, player)
        }

        if (event.view.title == "MarketPlace - Vente - Confirmation") {
            listingsInventoryService.clickOnAddNewItemConfirmation(event, player)
        }
    }

    // TODO : Refactoring common code between listings and mails
    private fun clickOnBottomLineListings(event: InventoryClickEvent, player: Player) {
        listingsInventoryService.clickOnSwitchPage(event, player)
        listingsInventoryService.clickOnSearch(event, player)
        listingsInventoryService.clickOnFilter(event, player)

        if (event.rawSlot == InventoryLoreEnum.MAIL.rawSlot) {
            val inventoryPaginated = mailsService.getPaginated(
                pagination = Pagination(
                    currentPlayer = player.uniqueId,
                    viewPlayer = player.uniqueId
                )
            )
            val mailsInventory = mailsInventoryService.initInventory(inventoryPaginated, player)
            player.openInventory(mailsInventory)
        }
    }

    private fun clickOnBottomLineMails(event: InventoryClickEvent, player: Player) {
        mailsInventoryService.clickOnSwitchPage(event, player)
        mailsInventoryService.clickOnSearch(event, player)
        mailsInventoryService.clickOnFilter(event, player)

        if (event.rawSlot == InventoryLoreEnum.LISTING.rawSlot) {
            val inventoryPaginated = listingsService.getPaginated(
                pagination = Pagination(
                    showAll = true,
                    currentPlayer = player.uniqueId,
                    viewPlayer = player.uniqueId
                )
            )
            val listingsInventory = listingsInventoryService.initInventory(inventoryPaginated, player)
            player.openInventory(listingsInventory)
        }
    }

}
