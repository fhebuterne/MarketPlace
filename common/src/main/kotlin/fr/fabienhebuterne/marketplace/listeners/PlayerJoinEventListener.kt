package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.kodein.di.DI
import org.kodein.di.instance

class PlayerJoinEventListener(kodein: DI) : Listener {

    private val listingsService: ListingsService by kodein.instance()
    private val mailsService: MailsService by kodein.instance()

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        listingsService.updatePseudo(event.player)
        mailsService.updatePseudo(event.player)
    }

}
