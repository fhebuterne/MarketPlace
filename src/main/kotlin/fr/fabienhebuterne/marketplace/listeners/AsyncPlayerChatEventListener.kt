package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.utils.longIsValid
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class AsyncPlayerChatEventListener(val marketPlace: MarketPlace, kodein: Kodein) : Listener {

    private val marketService: MarketService by kodein.instance<MarketService>()

    @EventHandler
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        val rawSlot = marketService.playersWaitingCustomQuantity[event.player.uniqueId] ?: return

        event.isCancelled = true

        if (event.message.contains("cancel")) {
            marketService.playersWaitingCustomQuantity.remove(event.player.uniqueId)
            event.player.sendMessage("cancelled buying ...")
        } else if (!longIsValid(event.message)) {
            event.player.sendMessage("number is not valid retry")
        } else {
            marketService.buyItem(event.player, rawSlot, event.message.toInt(), true)
            marketService.playersWaitingCustomQuantity.remove(event.player.uniqueId)
        }
    }

}