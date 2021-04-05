package fr.fabienhebuterne.marketplace.commands.factory

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import fr.fabienhebuterne.marketplace.utils.BootstrapLoader
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.kodein.di.DI

// TODO : Move factory in external libs
/**
 * Init this factory with plugin instance
 *
 * @param instance Main class of your plugin
 */
class CallCommandFactoryInit<T : BootstrapLoader>(private val instance: T, private val baseCommand: String) {
    fun onCommand(
        commandSender: CommandSender,
        command: Command,
        commandLabel: String,
        args: Array<String>,
        classLoader: ClassLoader,
        commandPath: String,
        permissionPrefix: String,
        loadWithArgs: Boolean,
        kodein: DI
    ): Boolean {
        // TODO : Add boolean to choose use baseCommand or not (ex: use /namePlugin command or just /command)
        if (!baseCommand.equals(commandLabel, ignoreCase = true) && !command.aliases.contains(commandLabel)) {
            return true
        }
        val commandLowercase: String = if (!loadWithArgs) {
            command.name.toLowerCase()
        } else {
            if (args.isEmpty()) {
                val commandClassPathHelp = "$commandPath.CommandHelp"
                instanceCommand(
                    classLoader,
                    commandClassPathHelp,
                    kodein,
                    permissionPrefix,
                    "help",
                    commandSender,
                    commandLabel,
                    command,
                    args
                )
                return true
            }
            args[0].toLowerCase()
        }
        val commandName = commandLowercase.replaceFirst(
            commandLowercase[0].toString().toRegex(),
            commandLowercase[0].toString().toUpperCase()
        )
        val commandClassPath = "$commandPath.Command$commandName"
        try {
            instanceCommand(
                classLoader,
                commandClassPath,
                kodein,
                permissionPrefix,
                commandName,
                commandSender,
                commandLabel,
                command,
                args
            )
        } catch (ignored: CustomException) {
        } catch (ignored: ClassNotFoundException) {
            val commandClassPathHelp = "$commandPath.CommandHelp"
            instanceCommand(
                classLoader,
                commandClassPathHelp,
                kodein,
                permissionPrefix,
                commandName,
                commandSender,
                commandLabel,
                command,
                args
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun instanceCommand(
        classLoader: ClassLoader,
        commandClassPath: String,
        kodein: DI,
        permissionPrefix: String,
        commandName: String,
        commandSender: CommandSender,
        commandLabel: String,
        command: Command,
        args: Array<String>
    ) {
        @Suppress("UNCHECKED_CAST")
        val cmd: ICallCommand<BootstrapLoader> = classLoader.loadClass(commandClassPath).getConstructor(DI::class.java)
            .newInstance(kodein) as ICallCommand<BootstrapLoader>
        cmd.instance = instance
        cmd.permission = permissionPrefix + commandName
        if (commandSender is Player) {
            cmd.runAsPlayer(Bukkit.getServer(), commandSender, commandLabel, command, args)
        } else {
            cmd.run(Bukkit.getServer(), commandSender, commandLabel, command, args)
        }
    }

}
