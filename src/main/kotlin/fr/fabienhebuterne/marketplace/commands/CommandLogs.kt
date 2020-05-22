package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class CommandLogs(kodein: Kodein) : CallCommand<MarketPlace>("logs") {

    private val logsService: LogsService by kodein.instance<LogsService>()

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        val logsPaginated = logsService.getPaginated(player.uniqueId, 1, 0,10)

        // TODO : Send in chat message
        logsPaginated.results.forEach {
            formatLogMessage(player, it)
        }

    }

    private fun formatLogMessage(player: Player, logs: Logs) {
        //player.sendMessage("Le joueur %player% à %logType% ${logs.price} - ${logs.quantity}")



    }

}