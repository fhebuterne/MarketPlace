package fr.fabienhebuterne.marketplace.exceptions

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.command.CommandSender

class HandEmptyException(commandSender: CommandSender) : CustomException() {
    init {
        commandSender.sendMessage("§cYou need to have item in your hand !")
    }
}