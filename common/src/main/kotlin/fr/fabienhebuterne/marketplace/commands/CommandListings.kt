package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.services.MarketService
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI
import org.kodein.di.instance

class CommandListings(kodein: DI) : CallCommand<MarketPlace>("listings") {

    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()
    private val marketService: MarketService by kodein.instance<MarketService>()

    companion object {
        const val BIG_CHEST_SIZE = 54
    }

    override fun runFromPlayer(
        server: Server,
        player: Player,
        commandLabel: String,
        cmd: Command,
        args: Array<String>
    ) {
        // TODO : Put this in common code (callCommand)
        if (instance.isReload) {
            player.sendMessage(instance.tl.errors.reloadNotAvailable)
            return
        }

        marketService.playersWaitingDefinedQuantity.remove(player.uniqueId)

        var pagination = Pagination<Listings>(
                showAll = true,
                currentPlayer = player.uniqueId,
                viewPlayer = player.uniqueId
        )

        if (args.size > 1) {
            if (!player.hasPermission("marketplace.listings.other")) {
                player.sendMessage(instance.tl.errors.missingPermission)
                return
            }

            val uuid = listingsService.findUUIDBySellerPseudo(args[1])
            if (uuid == null) {
                player.sendMessage(instance.tl.errors.playerNotFound)
                return
            }

            pagination = pagination.copy(showAll = false, currentPlayer = uuid)
        }

        val listingsPaginated = listingsService.getPaginated(pagination = pagination)
        val initListingsInventory = listingsInventoryService.initInventory(listingsPaginated, player)
        player.openInventory(initListingsInventory)
    }

}
