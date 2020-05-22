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
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.*


class MarketService(private val marketPlace: MarketPlace,
                    private val listingsService: ListingsService,
                    private val listingsRepository: ListingsRepository,
                    private val mailsRepository: MailsRepository,
                    private val listingsInventoryService: ListingsInventoryService,
                    private val logsService: LogsService) {

    val playersWaitingCustomQuantity: MutableMap<UUID, Int> = mutableMapOf()

    fun buyItem(player: Player, rawSlot: Int, quantity: Int, showMessage: Boolean = false) {
        val paginationListings = listingsService.playersView[player.uniqueId]
        val listings = paginationListings?.results?.get(rawSlot) ?: return

        if (listings.quantity < quantity) {
            if (showMessage) {
                player.sendMessage("The requested quantity is no longer available...")
            }
            return
        }

        val listingsDatabase = listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)

        // TODO : custom exception
        if (listingsDatabase == null) {
            player.sendMessage("Item no exist ...")
            return
        }

        if (listingsDatabase.quantity < quantity) {
            player.sendMessage("The requested quantity is no longer available...")
            return
        }

        val needingMoney = listingsDatabase.price * quantity.toDouble()
        val hasMoney = marketPlace.getEconomy().has(Bukkit.getOfflinePlayer(player.uniqueId), needingMoney)

        if (!hasMoney) {
            throw NotEnoughMoneyException(player)
        }

        marketPlace.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.uniqueId), needingMoney)

        if (listingsDatabase.quantity > 1) {
            listingsRepository.update(listingsDatabase.copy(quantity = listingsDatabase.quantity - quantity))
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

        player.sendMessage("§aYou just bought $quantity of ${listingsDatabase.itemStack.type} for $needingMoney")
        val refreshInventory = listingsService.getPaginated(player.uniqueId, paginationListings.currentPage)
        player.openInventory(listingsInventoryService.initInventory(marketPlace, refreshInventory, player))
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
                    player.sendMessage("Please enter quantity (max available is: ${listings.quantity}) you want to get...")
                    player.sendMessage("If you want to cancel, write '§a§lcancel§r' in chat")
                    player.closeInventory()
                }
            }
        }
    }
}