package fr.fabienhebuterne.marketplace;

import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MarketPlace extends JavaPlugin {

    private CallCommandFactoryInit<MarketPlace> callCommandFactoryInit;

    @Override
    public void onEnable() {
        this.callCommandFactoryInit = new CallCommandFactoryInit<>(this, "marketplace");
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String commandLabel,
                             final String[] args) {
        return this.callCommandFactoryInit.onCommandCustomCraft(
                sender,
                command,
                commandLabel,
                args,
                MarketPlace.class.getClassLoader(),
                "fr.fabienhebuterne.marketplace.commands",
                "marketplace.",
                true
        );
    }

}
