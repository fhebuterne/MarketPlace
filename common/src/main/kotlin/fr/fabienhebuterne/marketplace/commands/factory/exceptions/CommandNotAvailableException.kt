package fr.fabienhebuterne.marketplace.commands.factory.exceptions

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandNotAvailableException(commandSender: CommandSender) : CustomException() {
    init {
        val mode = if (commandSender is Player) {
            "Player"
        } else {
            "Console"
        }

        commandSender.sendMessage("§cSorry, this command is not available for $mode !")
    }
}