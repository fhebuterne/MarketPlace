package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.exceptions.NotEnoughMoneyException
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MarketService(private val marketPlace: MarketPlace,
                    private val listingsService: ListingsService,
                    private val listingsRepository: ListingsRepository,
                    private val mailsRepository: MailsRepository,
                    private val inventoryInitService: InventoryInitService) {

    fun buyItem(player: Player, rawSlot: Int, quantity: Int) {
        val paginationListings = listingsService.playersView[player.uniqueId]
        val listings = paginationListings?.results?.get(rawSlot) ?: return

        if (listings.quantity < quantity) {
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
            listingsRepository.delete(listingsDatabase.id)
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

        player.sendMessage("§aYou just bought $quantity of ${listingsDatabase.itemStack.type} for $needingMoney")
        val refreshInventory = listingsService.getInventoryPaginated(player.uniqueId, paginationListings.currentPage)
        player.openInventory(inventoryInitService.listingsInventory(marketPlace, refreshInventory, player))
    }
}