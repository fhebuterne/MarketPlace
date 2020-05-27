package fr.fabienhebuterne.marketplace.exceptions

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import fr.fabienhebuterne.marketplace.tl
import org.bukkit.command.CommandSender

class HandEmptyException(commandSender: CommandSender) : CustomException() {
    init {
        commandSender.sendMessage(tl.errors.handEmpty)
    }
}