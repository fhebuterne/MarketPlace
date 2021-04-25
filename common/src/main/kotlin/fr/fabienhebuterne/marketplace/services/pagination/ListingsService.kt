package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadableString
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Clock
import java.time.Instant
import java.util.*

class ListingsService(
    private val marketPlace: MarketPlace,
    private val listingsRepository: ListingsRepository,
    private val logsService: LogsService,
    private val clock: Clock
) : PaginationService<Listings>(listingsRepository, marketPlace) {

    fun updateListings(findExistingListings: Listings, amount: Int, player: Player) {
        val updatedListings = findExistingListings.copy(
            quantity = findExistingListings.quantity + amount,
            auditData = findExistingListings.auditData.copy(
                updatedAt = Instant.now(clock).toEpochMilli(),
                expiredAt = Instant.now(clock).toEpochMilli() + (marketPlace.conf.expiration.listingsToMails * 1000)
            )
        )
        update(updatedListings)

        logsService.saveListingsLog(
            player = player,
            listings = updatedListings,
            quantity = amount,
            money = findExistingListings.price,
        )

        val listingUpdated =
            marketPlace.tl.listingUpdated.replace(
                ConfigPlaceholder.ADDED_QUANTITY.placeholder,
                amount.toString()
            )
                .replace(ConfigPlaceholder.ITEM_STACK.placeholder, findExistingListings.itemStack.type.toString())
                .replace(ConfigPlaceholder.TOTAL_QUANTITY.placeholder, updatedListings.quantity.toString())

        player.sendMessage(listingUpdated)
        player.inventory.setItemInMainHand(ItemStack(Material.AIR))
    }

    fun create(player: Player, listings: Listings) {
        create(listings)

        logsService.saveListingsLog(
            player = player,
            listings = listings,
            quantity = listings.quantity,
            money = listings.price,
        )

        val listingsCreatedMessage =
            marketPlace.tl.listingCreated.replace(ConfigPlaceholder.QUANTITY.placeholder, listings.quantity.toString())
                .replace(ConfigPlaceholder.ITEM_STACK.placeholder, listings.itemStack.type.toString())
                .replace(ConfigPlaceholder.UNIT_PRICE.placeholder, convertDoubleToReadableString(listings.price))

        player.sendMessage(listingsCreatedMessage)
        player.inventory.setItemInMainHand(ItemStack(Material.AIR))
    }

    fun findUUIDBySellerPseudo(sellerPseudo: String): UUID? = listingsRepository.findUUIDBySellerPseudo(sellerPseudo)

    fun updatePseudo(player: Player) {
        val findListingsByUUID = listingsRepository.findByUUID(player.uniqueId)
        val filterListings: List<Listings> = findListingsByUUID.filter { it.sellerPseudo != player.name }
        filterListings.map {
            it.copy(sellerPseudo = player.name)
        }.forEach {
            listingsRepository.update(it)
        }
    }

}
