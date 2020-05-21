package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
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
    private val listingsRepository: ListingsRepository by kodein.instance<ListingsRepository>()
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
            clickOnListingsInventory(event, player)
            clickOnBottomLineListings(event, player)
        }

        if (event.view.title == "MarketPlace - Mails") {
            clickOnBottomLineMails(event, player)
        }

        if (event.view.title == "MarketPlace - Vente - Confirmation") {
            clickOnAddNewItemConfirmation(event, player)
        }

    }

    // TODO : Refactoring common code between listings and mails
    private fun clickOnBottomLineListings(event: InventoryClickEvent, player: Player) {
        listingsInventoryService.clickOnSwitchPage(marketPlace, event, player)

        if (event.rawSlot == InventoryLoreEnum.MAIL.rawSlot) {
            val inventoryPaginated = mailsService.getInventoryPaginated(player.uniqueId, 1)
            val mailsInventory = mailsInventoryService.initInventory(marketPlace, inventoryPaginated, player)
            player.openInventory(mailsInventory)
        }
    }

    private fun clickOnBottomLineMails(event: InventoryClickEvent, player: Player) {
        mailsInventoryService.clickOnSwitchPage(marketPlace, event, player)

        if (event.rawSlot == InventoryLoreEnum.LISTING.rawSlot) {
            val inventoryPaginated = listingsService.getInventoryPaginated(player.uniqueId, 1)
            val listingsInventory = listingsInventoryService.initInventory(marketPlace, inventoryPaginated, player)
            player.openInventory(listingsInventory)
        }
    }

    private fun clickOnAddNewItemConfirmation(event: InventoryClickEvent, player: Player) {
        if (event.slotType != InventoryType.SlotType.CONTAINER) {
            return
        }

        if (event.rawSlot == 2) {
            val paginated = listingsInventoryService.playersConfirmation[player.uniqueId]
            if (paginated != null && paginated is Listings) {
                listingsRepository.create(paginated)
                listingsInventoryService.playersConfirmation.remove(player.uniqueId)
                player.sendMessage("created item after confirmation ok")
                player.itemInHand = ItemStack(Material.AIR)
            }
            player.closeInventory()
        }

        if (event.rawSlot == 6) {
            val listings = listingsInventoryService.playersConfirmation[player.uniqueId]
            if (listings != null) {
                listingsInventoryService.playersConfirmation.remove(player.uniqueId)
            }
            player.sendMessage("cancelled sell item")
            player.closeInventory()
        }
    }

    private fun clickOnListingsInventory(event: InventoryClickEvent, player: Player) {
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