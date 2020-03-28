package fr.fabienhebuterne.marketplace.commands.factory;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CallCommand<T extends JavaPlugin> implements ICallCommand<T> {
    private final transient String name;
    protected transient T instance;
    protected transient String permission;

    protected CallCommand(final String name) {
        this.name = name;
    }

    @Override
    public void setInstance(final T instance) {
        this.instance = instance;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public void setPermission(final String permission) {
        this.permission = permission;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run(final Server server,
                    final Player player,
                    final String commandLabel,
                    final Command cmd,
                    final String[] args) throws Exception {
        if (!player.hasPermission(permission)) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande !");
            return;
        }
        runFromPlayer(server, player, commandLabel, cmd, args);
    }

    protected void runFromPlayer(final Server server,
                                 final Player player,
                                 final String commandLabel,
                                 final Command cmd,
                                 final String[] args) throws Exception {

    }

    @Override
    public void run(final Server server,
                    final CommandSender commandSender,
                    final String commandLabel,
                    final Command cmd,
                    final String[] args) throws Exception {
        runFromOther(server, commandSender, commandLabel, cmd, args);
    }

    protected void runFromOther(final Server server,
                                final CommandSender commandSender,
                                final String commandLabel,
                                final Command cmd,
                                final String[] args) throws Exception {

    }

}
