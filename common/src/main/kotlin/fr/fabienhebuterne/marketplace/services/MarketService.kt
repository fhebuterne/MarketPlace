package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.exceptions.NotEnoughMoneyException
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadeableString
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.stream.IntStream

data class WaitingDefinedQuantity(
    val listings: Listings,
    val clickType: ClickType
)

class MarketService(
    private val marketPlace: MarketPlace,
    private val listingsService: ListingsService,
    private val listingsRepository: ListingsRepository,
    private val listingsInventoryService: ListingsInventoryService,
    private val mailsService: MailsService,
    private val mailsRepository: MailsRepository,
    private val mailsInventoryService: MailsInventoryService,
    private val logsService: LogsService,
    private val notificationService: NotificationService
) {

    val playersWaitingCustomQuantity: MutableMap<UUID, Int> = mutableMapOf()
    val playersWaitingDefinedQuantity: MutableMap<UUID, WaitingDefinedQuantity> = mutableMapOf()

    fun buyItem(player: Player, rawSlot: Int, quantity: Int, showMessage: Boolean = false) {
        val paginationListings = listingsService.playersView[player.uniqueId]
        val listings = paginationListings?.results?.get(rawSlot) ?: return

        if (listings.quantity < quantity) {
            if (showMessage) {
                player.sendMessage(marketPlace.tl.errors.quantityNotAvailable)
            }
            return
        }

        val listingsDatabase = listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)

        // TODO : custom exception
        if (listingsDatabase == null) {
            player.sendMessage(marketPlace.tl.errors.itemNotExist)
            return
        }

        if (listingsDatabase.quantity < quantity) {
            player.sendMessage(marketPlace.tl.errors.quantityNotAvailable)
            return
        }

        val needingMoney = listingsDatabase.price * quantity

        takeMoneyBuyer(player, needingMoney)
        giveMoneySeller(listingsDatabase.sellerUuid, needingMoney)

        val takeQuantity = listingsDatabase.quantity - quantity

        if (listingsDatabase.quantity > 0 && takeQuantity > 0) {
            listingsRepository.update(listingsDatabase.copy(quantity = takeQuantity))
        } else {
            listingsDatabase.id?.let { listingsRepository.delete(it) }
        }

        mailsService.saveListingsToMail(listingsDatabase, player, quantity)
        logsService.buyItemLog(player, listingsDatabase, quantity, needingMoney)
        notificationService.sellerItemNotification(listingsDatabase, quantity, needingMoney)

        val itemBuyMessage = marketPlace.tl.itemBuy.replace(ConfigPlaceholder.QUANTITY.placeholder, quantity.toString())
            .replace(ConfigPlaceholder.ITEM_STACK.placeholder, listingsDatabase.itemStack.type.toString())
            .replace(ConfigPlaceholder.PRICE.placeholder, needingMoney.toString())

        player.sendMessage(itemBuyMessage)
        val refreshInventory = listingsService.getPaginated(pagination = paginationListings)
        val initInventory = listingsInventoryService.initInventory(refreshInventory, player)
        Bukkit.getScheduler().runTask(marketPlace.loader, Runnable {
            listingsInventoryService.openInventory(player, initInventory)
        })
    }

    private fun giveMoneySeller(sellerUuid: UUID, money: Double) {
        marketPlace.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(sellerUuid), money)
    }

    private fun takeMoneyBuyer(player: Player, needingMoney: Double) {
        val hasMoney = marketPlace.getEconomy().has(Bukkit.getOfflinePlayer(player.uniqueId), needingMoney)

        if (!hasMoney) {
            throw NotEnoughMoneyException(player)
        }

        marketPlace.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.uniqueId), needingMoney)
    }

    fun clickOnListingsInventory(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot in 0..44) {
            if (event.currentItem == null || event.currentItem?.type == Material.AIR) {
                return
            }

            val paginationListings = listingsService.playersView[player.uniqueId]
            val listings = paginationListings?.results?.get(event.rawSlot) ?: return

            if (listings.sellerUuid != player.uniqueId) {
                clickToBuyItem(event, player, listings)

                if (player.hasPermission("marketplace.listings.other.remove")) {
                    clickToRemoveItem(event, player, listings, event.isShiftClick && event.isRightClick, true)
                }
            } else {
                clickToRemoveItem(event, player, listings, event.isShiftClick && event.isLeftClick)
            }
        }
    }

    private fun clickToRemoveItem(
        event: InventoryClickEvent,
        player: Player,
        listings: Listings,
        isClickValid: Boolean,
        isAdmin: Boolean = false
    ) {
        if (!isClickValid) {
            return
        }

        forwardListingsToMails(listings, player, event, isAdmin)

        val initInventory = listingsInventoryService.initInventory(
            listingsService.playersView[player.uniqueId]
                ?: Pagination(currentPlayer = player.uniqueId, viewPlayer = player.uniqueId), player
        )
        listingsInventoryService.openInventory(player, initInventory)
    }

    private fun forwardListingsToMails(
        listings: Listings,
        player: Player,
        event: InventoryClickEvent,
        isAdmin: Boolean
    ) {
        val listingsFind = listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)

        if (listingsFind == null) {
            player.sendMessage(marketPlace.tl.errors.itemNotExist)
            return
        }

        logsService.listingsToMailsLog(player, listings, isAdmin)

        listingsService.playersView[player.uniqueId] = listingsService.playersView[player.uniqueId]?.let {
            val elementToRemove = it.results.filterIndexed { index, _ -> index == event.rawSlot }
            val copy = listingsService.playersView[player.uniqueId]?.copy(
                results = it.results.minus(elementToRemove)
            )
            copy
        } ?: Pagination(currentPlayer = player.uniqueId, viewPlayer = player.uniqueId)

        listingsFind.id?.let { listingsRepository.delete(it) }
        mailsService.saveListingsToMail(listingsFind)
    }

    private fun clickToBuyItem(event: InventoryClickEvent, player: Player, listings: Listings) {
        confirmationBuyItem(
            event,
            player,
            listings,
            ClickType.LEFT,
            1,
            marketPlace.tl.listingItemBottomLoreSellerConfirmationLeftClick
        )

        confirmationBuyItem(
            event,
            player,
            listings,
            ClickType.RIGHT,
            64,
            marketPlace.tl.listingItemBottomLoreSellerConfirmationRightClick
        )

        if (event.click == ClickType.MIDDLE) {
            playersWaitingCustomQuantity[player.uniqueId] = event.rawSlot
            marketPlace.tl.clickMiddleListingInventory
                .map {
                    it.replace(ConfigPlaceholder.QUANTITY.placeholder, listings.quantity.toString())
                        .replace(ConfigPlaceholder.PRICE.placeholder, convertDoubleToReadeableString(listings.price))
                        .replace(ConfigPlaceholder.ITEM_STACK.placeholder, listings.itemStack.type.toString())
                }
                .forEach {
                    player.sendMessage(it)
                }
            player.closeInventory()
        }
    }

    private fun confirmationBuyItem(
        event: InventoryClickEvent,
        player: Player,
        listings: Listings,
        clickType: ClickType,
        quantity: Int,
        translationLore: List<String>
    ) {
        if (event.click != clickType) {
            return
        }

        if (listings.quantity < quantity) {
            return
        }

        if (playersWaitingDefinedQuantity[player.uniqueId] != null
            && playersWaitingDefinedQuantity[player.uniqueId] == WaitingDefinedQuantity(listings, clickType)
        ) {
            playersWaitingDefinedQuantity.remove(player.uniqueId)
            buyItem(player, event.rawSlot, quantity)
        } else {
            val itemStack: ItemStack =
                listingsInventoryService.setBaseBottomLore(listings.itemStack.clone(), listings, player)
            val itemMeta = itemStack.itemMeta
            val lore = itemMeta?.lore

            lore?.addAll(translationLore)

            itemMeta?.lore = lore
            itemStack.itemMeta = itemMeta
            event.currentItem = itemStack

            playersWaitingDefinedQuantity[player.uniqueId] = WaitingDefinedQuantity(listings, event.click)
        }
    }

    fun clickOnMailsInventory(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot in 0..44) {
            if (event.currentItem == null || event.currentItem?.type == Material.AIR) {
                return
            }

            if (event.isShiftClick && event.isRightClick && player.hasPermission("marketplace.mails.other.take")) {
                takeItem(player, event.rawSlot, true)
            }

            if (event.isLeftClick) {
                takeItem(player, event.rawSlot)
            }
        }
    }

    private fun takeItem(player: Player, rawSlot: Int, isAdmin: Boolean = false) {
        val paginationMails = mailsService.playersView[player.uniqueId]
        val mail = paginationMails?.results?.get(rawSlot) ?: return

        val mails = mailsService.find(mail.id.toString())

        if (mails == null) {
            player.sendMessage(marketPlace.tl.errors.itemNotExist)
            return
        }

        val slotInventoryAvailable = player.inventory.storageContents.clone()
            .filter { it == null || it.type == Material.AIR }
            .count()

        val itemPresentSlotAvailable: Int = player.inventory.storageContents.clone()
            .filterNotNull()
            .filter { it.isSimilar(mail.itemStack) }
            .filter { it.amount < it.maxStackSize }
            .sumBy { it.maxStackSize - it.amount }

        val itemStack = mail.itemStack.clone()
        val maxQuantityInventoryAvailable = slotInventoryAvailable * itemStack.maxStackSize + itemPresentSlotAvailable

        val amountItemStack = if (mail.quantity > maxQuantityInventoryAvailable) {
            maxQuantityInventoryAvailable
        } else {
            mail.quantity
        }

        if (slotInventoryAvailable == 0 && itemPresentSlotAvailable == 0) {
            player.sendMessage(marketPlace.tl.errors.inventoryFull)
            return
        }

        if (mail.playerUuid != player.uniqueId && !isAdmin) {
            player.sendMessage("§cOperation not allowed ...")
            return
        }

        IntStream.range(0, amountItemStack)
            .forEach {
                player.inventory.addItem(itemStack)
            }

        logsService.takeItemLog(player, mail, amountItemStack, isAdmin)

        if (mail.quantity - amountItemStack <= 0) {
            mail.id?.let { mailsRepository.delete(it) }
        } else {
            mailsRepository.update(mail.copy(quantity = mail.quantity - amountItemStack))
        }

        val refreshInventory = mailsService.getPaginated(pagination = paginationMails)
        val initInventory = mailsInventoryService.initInventory(refreshInventory, player)
        mailsInventoryService.openInventory(player, initInventory)
    }
}
