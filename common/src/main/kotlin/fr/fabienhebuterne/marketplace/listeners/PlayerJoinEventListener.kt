package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.event.player.PlayerJoinEvent
import org.kodein.di.DI
import org.kodein.di.instance

class PlayerJoinEventListener(kodein: DI) : BaseListener<PlayerJoinEvent>() {

    private val listingsService: ListingsService by kodein.instance()
    private val mailsService: MailsService by kodein.instance()

    override fun execute(event: PlayerJoinEvent) {
        listingsService.updatePseudo(event.player)
        mailsService.updatePseudo(event.player)
    }

}
