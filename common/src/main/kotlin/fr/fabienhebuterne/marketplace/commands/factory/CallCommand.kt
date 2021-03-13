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
            player.sendMessage(instance.missingPermissionMessage)
            return
        }

        if (instance.isReload) {
            player.sendMessage(instance.reloadNotAvailableMessage)
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
        if (instance.isReload) {
            commandSender.sendMessage(instance.reloadNotAvailableMessage)
            return
        }

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
