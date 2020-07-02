package fr.fabienhebuterne.marketplace.commands.factory

import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

interface ICallCommand<T : JavaPlugin> {
    /**
     * @return Command name
     */
    val name: String

    /**
     * @return T plugin intance
     */
    var instance: T

    /**
     * @return permission
     */
    var permission: String

    /**
     * Player command use this method
     * @param server
     * @param player
     * @param commandLabel
     * @param cmd
     * @param args
     */
    fun run(server: Server,
            player: Player,
            commandLabel: String,
            cmd: Command,
            args: Array<String>)

    /**
     * Other entities use this, like console, commandblocks...
     * @param server
     * @param commandSender
     * @param commandLabel
     * @param cmd
     * @param args
     */
    fun run(server: Server,
            commandSender: CommandSender,
            commandLabel: String,
            cmd: Command,
            args: Array<String>)
}