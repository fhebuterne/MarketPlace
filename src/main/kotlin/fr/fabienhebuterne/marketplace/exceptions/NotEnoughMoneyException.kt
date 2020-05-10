package fr.fabienhebuterne.marketplace.exceptions

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.command.CommandSender

class NotEnoughMoneyException(commandSender: CommandSender) : CustomException() {
    init {
        commandSender.sendMessage("Not enough money to do this action")
    }
}