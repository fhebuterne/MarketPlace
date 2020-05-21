package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinEventListener(val marketPlace: MarketPlace, private val listingsRepository: ListingsRepository) : Listener {

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val findByUUID = listingsRepository.findByUUID(event.player.uniqueId)
        val filterListings: List<Listings> = findByUUID.filter { it.sellerPseudo != event.player.name }
        filterListings.map {
            it.copy(sellerPseudo = event.player.name)
        }.forEach {
            listingsRepository.update(it)
        }
    }

}