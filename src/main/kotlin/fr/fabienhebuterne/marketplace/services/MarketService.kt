package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.exceptions.NotEnoughMoneyException
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import fr.fabienhebuterne.marketplace.tl
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.*


class MarketService(private val marketPlace: MarketPlace,
                    private val listingsService: ListingsService,
                    private val listingsRepository: ListingsRepository,
                    private val mailsService: MailsService,
                    private val mailsRepository: MailsRepository,
                    private val listingsInventoryService: ListingsInventoryService,
                    private val logsService: LogsService) {

    val playersWaitingCustomQuantity: MutableMap<UUID, Int> = mutableMapOf()

    fun buyItem(player: Player, rawSlot: Int, quantity: Int, showMessage: Boolean = false) {
        val paginationListings = listingsService.playersView[player.uniqueId]
        val listings = paginationListings?.results?.get(rawSlot) ?: return

        if (listings.quantity < quantity) {
            if (showMessage) {
                player.sendMessage(tl.errors.quantityNotAvailable)
            }
            return
        }

        val listingsDatabase = listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)

        // TODO : custom exception
        if (listingsDatabase == null) {
            player.sendMessage(tl.errors.itemNotExist)
            return
        }

        if (listingsDatabase.quantity < quantity) {
            player.sendMessage(tl.errors.quantityNotAvailable)
            return
        }

        val needingMoney = listingsDatabase.price * quantity.toDouble()

        takeMoneyBuyer(player, needingMoney)
        giveMoneySeller(player, needingMoney)

        val takeQuantity = listingsDatabase.quantity - quantity

        if (listingsDatabase.quantity > 1 && takeQuantity > 1) {
            listingsRepository.update(listingsDatabase.copy(quantity = takeQuantity))
        } else {
            listingsDatabase.id?.let { listingsRepository.delete(it) }
        }


        val mailsDatabase = mailsRepository.find(player.uniqueId.toString(), listingsDatabase.itemStack)

        if (mailsDatabase == null) {
            mailsRepository.create(
                    Mails(
                            playerUuid = player.uniqueId.toString(),
                            itemStack = listings.itemStack,
                            quantity = quantity,
                            auditData = AuditData(
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                    expiredAt = System.currentTimeMillis() + (3600 * 24 * 7 * 1000)
                            )
                    )
            )
        } else {
            mailsRepository.update(
                    mailsDatabase.copy(quantity = mailsDatabase.quantity + quantity)
            )
        }

        logsService.createFrom(
                player,
                listingsDatabase,
                quantity,
                needingMoney.toLong(),
                logType = LogType.BUY,
                fromLocation = Location.PLAYER_INVENTORY,
                toLocation = Location.LISTING_INVENTORY
        )

        // TODO : Send notif to seller when item is buyed (executed command with config)

        val itemBuyMessage = tl.itemBuy.replace("{{quantity}}", quantity.toString())
                .replace("{{item}}", listingsDatabase.itemStack.type.toString())
                .replace("{{price}}", needingMoney.toString())

        player.sendMessage(itemBuyMessage)
        val refreshInventory = listingsService.getPaginated(player.uniqueId, pagination = paginationListings)
        player.openInventory(listingsInventoryService.initInventory(marketPlace, refreshInventory, player))
    }

    private fun giveMoneySeller(player: Player, money: Double) {
        marketPlace.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.uniqueId), money)
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
            if (event.currentItem.type == Material.AIR) {
                return
            }

            if (event.isLeftClick) {
                buyItem(player, event.rawSlot, 1)
            }

            if (event.isRightClick) {
                buyItem(player, event.rawSlot, 64)
            }

            if (event.click == ClickType.MIDDLE) {
                val listings = listingsService.playersView[player.uniqueId]?.results?.get(event.rawSlot)
                if (listings != null) {
                    playersWaitingCustomQuantity[player.uniqueId] = event.rawSlot
                    player.sendMessage(tl.clickMiddleListingInventoryOne.replace("{{maxQuantity}}", listings.quantity.toString()))
                    player.sendMessage(tl.clickMiddleListingInventoryTwo)
                    player.closeInventory()
                }
            }
        }
    }

    fun clickOnMailsInventory(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot in 0..44) {
            if (event.currentItem.type == Material.AIR) {
                return
            }

            if (event.isLeftClick) {
                takeItem(player, event.rawSlot)
            }
        }
    }

    private fun takeItem(player: Player, rawSlot: Int) {
        val paginationMails = mailsService.playersView[player.uniqueId]
        val mail = paginationMails?.results?.get(rawSlot) ?: return

        val slotInventoryAvailable = player.inventory.contents.clone()
                .filter { it == null || it.type == Material.AIR }
                .count()

        val itemPresentSlotAvailable: Int = player.inventory.contents.clone()
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
            player.sendMessage(tl.errors.inventoryFull)
            return
        }

        itemStack.amount = amountItemStack
        player.inventory.addItem(itemStack)

        if (mail.quantity - amountItemStack <= 0) {
            mail.id?.let { mailsRepository.delete(it) }
        } else {
            mailsRepository.update(mail.copy(quantity = mail.quantity - amountItemStack))
        }

        player.closeInventory()
    }
}