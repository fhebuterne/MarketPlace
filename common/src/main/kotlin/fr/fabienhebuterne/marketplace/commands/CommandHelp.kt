package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI

class CommandHelp(kodein: DI) : CallCommand<MarketPlace>("help") {

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

        instance.tl.commandHelp.forEach {
            player.sendMessage(it)
        }
    }

}
