package fr.fabienhebuterne.marketplace.commands.factory

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CommandNotAvailableException
import fr.fabienhebuterne.marketplace.utils.BootstrapLoader
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class CallCommand<T : BootstrapLoader>(override val name: String) : ICallCommand<T> {

    override lateinit var instance: T
    override lateinit var permission: String

    override fun run(
            server: Server,
            player: Player,
            commandLabel: String,
            cmd: Command,
            args: Array<String>
    ) {
        if (!player.hasPermission(permission)) {
            // TODO : Put instance.tl.errors.missingPermission when domain and MarketPlace.kt will be splitted on multi-module
            // Need to have Translation object on BootstrapLoader interface
            player.sendMessage("§8[§6MarketPlace§8] §cYou don''t have permission to do this.")
            return
        }
        runFromPlayer(server, player, commandLabel, cmd, args)
    }

    protected open fun runFromPlayer(server: Server,
                                     player: Player,
                                     commandLabel: String,
                                     cmd: Command,
                                     args: Array<String>) {
        throw CommandNotAvailableException(player)
    }

    override fun run(server: Server,
                     commandSender: CommandSender,
                     commandLabel: String,
                     cmd: Command,
                     args: Array<String>) {
        runFromOther(server, commandSender, commandLabel, cmd, args)
    }

    protected open fun runFromOther(server: Server,
                                    commandSender: CommandSender,
                                    commandLabel: String,
                                    cmd: Command,
                                    args: Array<String>) {
        throw CommandNotAvailableException(commandSender)
    }

}
