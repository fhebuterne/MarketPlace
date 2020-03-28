package fr.fabienhebuterne.marketplace.commands.factory

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CommandNotAvailableException
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

abstract class CallCommand<T : JavaPlugin>(override val name: String) : ICallCommand<T> {

    override lateinit var instance: T
    override lateinit var permission: String

    override fun run(server: Server,
                     player: Player,
                     commandLabel: String,
                     cmd: Command,
                     args: Array<String>) {
        if (!player.hasPermission(permission)) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande !")
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