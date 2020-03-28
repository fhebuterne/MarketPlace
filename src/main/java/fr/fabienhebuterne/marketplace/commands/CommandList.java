package fr.fabienhebuterne.marketplace.commands;

import fr.fabienhebuterne.marketplace.MarketPlace;
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class CommandList extends CallCommand<MarketPlace> {
    protected CommandList() {
        super("list");
    }

    @Override
    protected void runFromPlayer(Server server, Player player, String commandLabel, Command cmd, String[] args) throws Exception {

    }
}
