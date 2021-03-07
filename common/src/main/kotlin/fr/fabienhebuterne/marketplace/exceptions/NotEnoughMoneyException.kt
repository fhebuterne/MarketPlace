package fr.fabienhebuterne.marketplace.exceptions

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.command.CommandSender

lateinit var notEnoughMoneyTranslation: String

class NotEnoughMoneyException(commandSender: CommandSender) : CustomException() {
    init {
        commandSender.sendMessage(notEnoughMoneyTranslation)
    }
}

fun loadNotEnoughMoneyExceptionTranslation(notEnoughMoney: String) {
    notEnoughMoneyTranslation = notEnoughMoney
}
