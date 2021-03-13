package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.kodein.di.DI
import org.kodein.di.instance

class PlayerJoinEventListener(kodein: DI) : Listener {

    private val listingsRepository: ListingsRepository by kodein.instance()
    private val mailsRepository: MailsRepository by kodein.instance()

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        // TODO : Create function in service
        val findListingsByUUID = listingsRepository.findByUUID(event.player.uniqueId)
        val filterListings: List<Listings> = findListingsByUUID.filter { it.sellerPseudo != event.player.name }
        filterListings.map {
            it.copy(sellerPseudo = event.player.name)
        }.forEach {
            listingsRepository.update(it)
        }

        val findMailsByUUID = mailsRepository.findByUUID(event.player.uniqueId)
        val filterMails: List<Mails> = findMailsByUUID.filter { it.playerPseudo != event.player.name }
        filterMails.map {
            it.copy(playerPseudo = event.player.name)
        }.forEach {
            mailsRepository.update(it)
        }
    }

}
