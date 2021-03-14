package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
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
    private val logsService: LogsService
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
        giveMoneySeller(player, needingMoney)

        val takeQuantity = listingsDatabase.quantity - quantity

        if (listingsDatabase.quantity > 1 && takeQuantity > 1) {
            listingsRepository.update(listingsDatabase.copy(quantity = takeQuantity))
        } else {
            listingsDatabase.id?.let { listingsRepository.delete(it) }
        }


        val mailsDatabase = mailsRepository.find(player.uniqueId, listingsDatabase.itemStack)

        if (mailsDatabase == null) {
            mailsRepository.create(
                Mails(
                    playerUuid = player.uniqueId,
                    playerPseudo = player.name,
                    itemStack = listings.itemStack,
                    quantity = quantity,
                    auditData = AuditData(
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        expiredAt = System.currentTimeMillis() + (marketPlace.configService.getSerialization().expiration.listingsToMails * 1000)
                    ),
                    version = marketPlace.itemStackReflection.getVersion()
                )
            )
        } else {
            mailsRepository.update(
                mailsDatabase.copy(quantity = mailsDatabase.quantity + quantity)
            )
        }

        logsService.createFrom(
            player = player,
            paginated = listingsDatabase,
            quantity = quantity,
            needingMoney = needingMoney,
            logType = LogType.BUY,
            fromLocation = Location.LISTING_INVENTORY,
            toLocation = Location.MAIL_INVENTORY
        )


        marketPlace.configService.getSerialization().sellerItemNotifCommand.forEach { command ->
            val commandReplace = command.replace("{{playerPseudo}}", listingsDatabase.sellerPseudo)
                .replace("{{playerUUID}}", listingsDatabase.sellerUuid.toString())
                .replace("{{quantity}}", quantity.toString())
                .replace("{{itemStack}}", listingsDatabase.itemStack.type.toString())
                .replace("{{totalPrice}}", needingMoney.toString())

            if (Bukkit.isPrimaryThread()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
            } else {
                Bukkit.getScheduler().runTaskLater(marketPlace.loader, Runnable {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
                }, 20L)
            }
        }

        val itemBuyMessage = marketPlace.tl.itemBuy.replace("{{quantity}}", quantity.toString())
            .replace("{{item}}", listingsDatabase.itemStack.type.toString())
            .replace("{{price}}", needingMoney.toString())

        player.sendMessage(itemBuyMessage)
        val refreshInventory = listingsService.getPaginated(pagination = paginationListings)
        player.openInventory(listingsInventoryService.initInventory(refreshInventory, player))
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
        player.openInventory(initInventory)
    }

    private fun forwardListingsToMails(
        listings: Listings,
        player: Player,
        event: InventoryClickEvent,
        isAdmin: Boolean
    ) {
        val listingsFind = listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)

        if (listingsFind == null) {
            // TODO : throw exception here
            player.sendMessage(marketPlace.tl.errors.itemNotExist)
            return
        }

        if (!isAdmin) {
            logsService.createFrom(
                player = player,
                paginated = listings,
                quantity = listings.quantity,
                needingMoney = null,
                logType = LogType.CANCEL,
                fromLocation = Location.LISTING_INVENTORY,
                toLocation = Location.MAIL_INVENTORY
            )
        } else {
            logsService.createFrom(
                player = Bukkit.getOfflinePlayer(listings.sellerUuid),
                adminPlayer = player,
                paginated = listings,
                quantity = listings.quantity,
                needingMoney = null,
                logType = LogType.CANCEL,
                fromLocation = Location.LISTING_INVENTORY,
                toLocation = Location.MAIL_INVENTORY
            )
        }

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
                    it.replace("{{maxQuantity}}", listings.quantity.toString())
                        .replace("{{price}}", convertDoubleToReadeableString(listings.price))
                        .replace("{{itemStack}}", listings.itemStack.type.toString())
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

        if (isAdmin) {
            logsService.createFrom(
                player = Bukkit.getOfflinePlayer(mail.playerUuid),
                adminPlayer = player,
                paginated = mail,
                quantity = amountItemStack,
                needingMoney = null,
                logType = LogType.GET,
                fromLocation = Location.MAIL_INVENTORY,
                toLocation = Location.PLAYER_INVENTORY
            )
        } else {
            logsService.createFrom(
                player = Bukkit.getOfflinePlayer(mail.playerUuid),
                paginated = mail,
                quantity = amountItemStack,
                needingMoney = null,
                logType = LogType.GET,
                fromLocation = Location.MAIL_INVENTORY,
                toLocation = Location.PLAYER_INVENTORY
            )
        }

        if (mail.quantity - amountItemStack <= 0) {
            mail.id?.let { mailsRepository.delete(it) }
        } else {
            mailsRepository.update(mail.copy(quantity = mail.quantity - amountItemStack))
        }

        val refreshInventory = mailsService.getPaginated(pagination = paginationMails)
        player.openInventory(mailsInventoryService.initInventory(refreshInventory, player))
    }
}
