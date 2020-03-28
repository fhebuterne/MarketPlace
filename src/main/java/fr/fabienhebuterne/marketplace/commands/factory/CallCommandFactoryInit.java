package fr.fabienhebuterne.marketplace.commands.factory;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

// TODO : Move factory in external libs
public class CallCommandFactoryInit<T extends JavaPlugin> {

    private T instance;
    private String baseCommand;

    /**
     * Init this factory with plugin instance
     *
     * @param instance Main class of your plugin
     */
    public CallCommandFactoryInit(T instance, String baseCommand) {
        this.instance = instance;
        this.baseCommand = baseCommand;
    }

    public boolean onCommandCustomCraft(final CommandSender commandSender,
                                        final Command command,
                                        final String commandLabel,
                                        final String[] args,
                                        final ClassLoader classLoader,
                                        String commandPath,
                                        final String permissionPrefix,
                                        boolean loadWithArgs) {
        // TODO : Add boolean to choose use baseCommand or not (ex: use /namePlugin command or just /command)
        if (!baseCommand.equalsIgnoreCase(commandLabel)) {
            return true;
        }

        String commandLowercase;

        if (!loadWithArgs) {
            commandLowercase = command.getName().toLowerCase();
        } else {
            if (args.length == 0) {
                return true;
            }
            commandLowercase = args[0].toLowerCase();
        }

        String commandName = commandLowercase.replaceFirst(
                String.valueOf(commandLowercase.charAt(0)),
                String.valueOf(commandLowercase.charAt(0)).toUpperCase()
        );
        String commandClassPath = commandPath + ".Command" + commandName;

        try {
            ICallCommand<T> cmd = (ICallCommand<T>) classLoader.loadClass(commandClassPath).newInstance();
            cmd.setInstance(instance);
            cmd.setPermission(permissionPrefix + commandName);

            if (!(commandSender instanceof Player)) {
                cmd.run(getServer(), commandSender, commandLabel, command, args);
            } else {
                cmd.run(getServer(), (Player) commandSender, commandLabel, command, args);
            }
        } catch (CustomException | ClassNotFoundException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
