package fr.fabienhebuterne.marketplace.exceptions

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.command.CommandSender

lateinit var emptyHandTranslation: String

class EmptyHandException(commandSender: CommandSender) : CustomException() {
    init {
        commandSender.sendMessage(emptyHandTranslation)
    }
}

fun loadEmptyHandExceptionTranslation(emptyHandMessage: String) {
    emptyHandTranslation = emptyHandMessage
}
