package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player

class CommandList : CallCommand<MarketPlace>("list") {

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        println("command list ok !")
        player.sendMessage("test ok !")
    }

}