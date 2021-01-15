package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.tl
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadeableString
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class ListingsService(
    private val marketPlace: MarketPlace,
    private val listingsRepository: ListingsRepository,
    private val logsService: LogsService
) : PaginationService<Listings>(listingsRepository) {

    fun updateListings(findExistingListings: Listings, currentItemStack: ItemStack, player: Player) {
        val updatedListings = findExistingListings.copy(
            quantity = findExistingListings.quantity + currentItemStack.amount,
            auditData = findExistingListings.auditData.copy(
                updatedAt = System.currentTimeMillis(),
                expiredAt = System.currentTimeMillis() + (marketPlace.configService.getSerialization().expiration.listingsToMails * 1000)
            )
        )
        update(updatedListings)

        logsService.createFrom(
            player = player,
            paginated = updatedListings,
            quantity = currentItemStack.amount,
            needingMoney = findExistingListings.price,
            logType = LogType.SELL,
            fromLocation = Location.PLAYER_INVENTORY,
            toLocation = Location.LISTING_INVENTORY
        )

        val listingUpdated = tl.listingUpdated.replace("{{quantityAdded}}", currentItemStack.amount.toString())
            .replace("{{item}}", findExistingListings.itemStack.type.toString())
            .replace("{{quantityTotal}}", updatedListings.quantity.toString())

        player.sendMessage(listingUpdated)
        player.inventory.itemInMainHand = ItemStack(Material.AIR)
    }

    fun create(player: Player, listings: Listings) {
        create(listings)

        logsService.createFrom(
            player = player,
            paginated = listings,
            quantity = listings.quantity,
            needingMoney = listings.price,
            logType = LogType.SELL,
            fromLocation = Location.PLAYER_INVENTORY,
            toLocation = Location.LISTING_INVENTORY
        )

        val listingsCreatedMessage = tl.listingCreated.replace("{{quantity}}", listings.quantity.toString())
            .replace("{{item}}", listings.itemStack.type.toString())
            .replace("{{unitPrice}}", convertDoubleToReadeableString(listings.price))

        player.sendMessage(listingsCreatedMessage)
        player.inventory.itemInMainHand = ItemStack(Material.AIR)
    }

    fun findUUIDBySellerPseudo(sellerPseudo: String): UUID? = listingsRepository.findUUIDBySellerPseudo(sellerPseudo)

}
