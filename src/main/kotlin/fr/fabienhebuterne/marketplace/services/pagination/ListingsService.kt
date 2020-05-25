package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ListingsService(private val listingsRepository: ListingsRepository, private val logsService: LogsService) : PaginationService<Listings>(listingsRepository) {
    fun updateListings(findExistingListings: Listings, currentItemStack: ItemStack, player: Player) {
        val updatedListings = findExistingListings.copy(
                quantity = findExistingListings.quantity + currentItemStack.amount,
                auditData = findExistingListings.auditData.copy(
                        updatedAt = System.currentTimeMillis(),
                        expiredAt = System.currentTimeMillis() + (3600 * 24 * 7 * 1000)
                )
        )
        listingsRepository.update(updatedListings)

        logsService.createFrom(
                player,
                updatedListings,
                currentItemStack.amount,
                null,
                LogType.SELL,
                fromLocation = Location.PLAYER_INVENTORY,
                toLocation = Location.LISTING_INVENTORY
        )

        player.sendMessage("updated item OK !")
        player.itemInHand = ItemStack(Material.AIR)
    }

    fun create(player: Player, listings: Listings) {
        listingsRepository.create(listings)

        logsService.createFrom(
                player,
                listings,
                listings.quantity,
                listings.price,
                LogType.SELL,
                fromLocation = Location.PLAYER_INVENTORY,
                toLocation = Location.LISTING_INVENTORY
        )

        player.sendMessage("created item after confirmation ok")
        player.itemInHand = ItemStack(Material.AIR)
    }
}