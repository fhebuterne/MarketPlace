package fr.fabienhebuterne.marketplace.exceptions

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.command.CommandSender

class BadArgumentException(commandSender: CommandSender, message: String) : CustomException() {
    init {
        commandSender.sendMessage(message)
    }
}