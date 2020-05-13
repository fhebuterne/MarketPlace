package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.services.InventoryInitService
import fr.fabienhebuterne.marketplace.services.ListingsService
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class CommandListings(kodein: Kodein) : CallCommand<MarketPlace>("listings") {

    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val inventoryInitService: InventoryInitService by kodein.instance<InventoryInitService>()

    companion object {
        const val BIG_CHEST_SIZE = 54
    }

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        val listingsPaginated = listingsService.getListingsPaginated(player.uniqueId, 1)

        val initListingsInventory = inventoryInitService.listingsInventory(instance, listingsPaginated, player)
        player.openInventory(initListingsInventory)
    }

}