package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.tl
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class CommandListings(kodein: Kodein) : CallCommand<MarketPlace>("listings") {

    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()

    companion object {
        const val BIG_CHEST_SIZE = 54
    }

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        // TODO : Put this in common code (callCommand)
        if (MarketPlace.isReload) {
            player.sendMessage(tl.errors.reloadNotAvailable)
            return
        }

        listingsInventoryService.playersWaitingRemove.remove(player.uniqueId)
        val listingsPaginated = listingsService.getPaginated(player.uniqueId)
        val initListingsInventory = listingsInventoryService.initInventory(instance, listingsPaginated, player)
        player.openInventory(initListingsInventory)
    }

}