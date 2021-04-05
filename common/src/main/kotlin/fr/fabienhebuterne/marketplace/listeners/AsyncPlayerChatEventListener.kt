package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import fr.fabienhebuterne.marketplace.exceptions.BadArgumentException
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.utils.intIsValid
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.kodein.di.DI
import org.kodein.di.instance
import java.text.MessageFormat

class AsyncPlayerChatEventListener(private val marketPlace: MarketPlace, kodein: DI) : Listener {

    private val marketService: MarketService by kodein.instance<MarketService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()
    private val mailsInventoryService: MailsInventoryService by kodein.instance<MailsInventoryService>()

    @EventHandler
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        try {
            execute(event)
        } catch (ignored: CustomException) {
            // We ignore CustomException because error msg send to player
            // and don't want to have stacktrace on console
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // TODO: Factory to put in common code for hidden exception
    private fun execute(event: AsyncPlayerChatEvent) {
        // Buy custom quantity
        val rawSlot = marketService.playersWaitingCustomQuantity[event.player.uniqueId]
        if (rawSlot != null) {
            event.isCancelled = true
            if (event.message.contains("cancel")) {
                marketService.playersWaitingCustomQuantity.remove(event.player.uniqueId)
                event.player.sendMessage(marketPlace.tl.cancelBuying)
            } else if (!intIsValid(event.message)) {
                throw BadArgumentException(
                    event.player,
                    MessageFormat.format(marketPlace.tl.errors.numberNotValid, event.message)
                )
            } else {
                marketService.buyItem(event.player, rawSlot, event.message.toInt(), true)
                marketService.playersWaitingCustomQuantity.remove(event.player.uniqueId)
            }
        }

        // Do a custom search
        if (listingsInventoryService.playersWaitingSearch.contains(event.player.uniqueId)) {
            listingsInventoryService.searchItemstack(event, true)
        }

        if (mailsInventoryService.playersWaitingSearch.contains(event.player.uniqueId)) {
            mailsInventoryService.searchItemstack(event, false)
        }
    }

}
