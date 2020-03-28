package fr.fabienhebuterne.marketplace.commands.factory;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface ICallCommand<T extends JavaPlugin> {
    /**
     * @return Command name
     */
    String getName();

    /**
     * Player command use this method
     * @param server
     * @param player
     * @param commandLabel
     * @param cmd
     * @param args
     */
    void run(final Server server,
             final Player player,
             final String commandLabel,
             final Command cmd,
             final String[] args) throws Exception;

    /**
     * Other entities use this, like console, commandblocks...
     * @param server
     * @param commandSender
     * @param commandLabel
     * @param cmd
     * @param args
     */
    void run(final Server server,
             final CommandSender commandSender,
             final String commandLabel,
             final Command cmd,
             final String[] args) throws Exception;

    void setInstance(final T instance);

    T getInstance();

    void setPermission(String permission);
}
