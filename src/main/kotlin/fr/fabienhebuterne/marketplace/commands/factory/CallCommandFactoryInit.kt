package fr.fabienhebuterne.marketplace.commands.factory

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

// TODO : Move factory in external libs
/**
 * Init this factory with plugin instance
 *
 * @param instance Main class of your plugin
 */
class CallCommandFactoryInit<T : JavaPlugin>(private val instance: T, private val baseCommand: String) {
    fun onCommand(commandSender: CommandSender,
                  command: Command,
                  commandLabel: String,
                  args: Array<String>,
                  classLoader: ClassLoader,
                  commandPath: String,
                  permissionPrefix: String,
                  loadWithArgs: Boolean): Boolean {
        // TODO : Add boolean to choose use baseCommand or not (ex: use /namePlugin command or just /command)
        if (!baseCommand.equals(commandLabel, ignoreCase = true)) {
            return true
        }
        val commandLowercase: String = if (!loadWithArgs) {
            command.name.toLowerCase()
        } else {
            if (args.isEmpty()) {
                return true
            }
            args[0].toLowerCase()
        }
        val commandName = commandLowercase.replaceFirst(commandLowercase[0].toString().toRegex(),
                commandLowercase[0].toString().toUpperCase()
        )
        val commandClassPath = "$commandPath.Command$commandName"
        try {
            @Suppress("UNCHECKED_CAST")
            val cmd: ICallCommand<T> = classLoader.loadClass(commandClassPath).newInstance() as ICallCommand<T>
            cmd.instance = instance
            cmd.permission = permissionPrefix + commandName
            if (commandSender is Player) {
                cmd.run(Bukkit.getServer(), commandSender, commandLabel, command, args)
            } else {
                cmd.run(Bukkit.getServer(), commandSender, commandLabel, command, args)
            }
        } catch (ignored: CustomException) {
        } catch (ignored: ClassNotFoundException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

}