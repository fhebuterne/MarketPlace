package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadableString
import org.bukkit.Bukkit

class NotificationService(val marketPlace: MarketPlace) {

    fun sellerItemNotification(
        listingsDatabase: Listings,
        quantity: Int,
        needingMoney: Double
    ) {
        marketPlace.conf.sellerItemNotifCommand.forEach { command ->
            val commandReplace =
                command.replace(ConfigPlaceholder.PLAYER_PSEUDO.placeholder, listingsDatabase.sellerPseudo)
                    .replace(ConfigPlaceholder.PLAYER_UUID.placeholder, listingsDatabase.sellerUuid.toString())
                    .replace(ConfigPlaceholder.QUANTITY.placeholder, quantity.toString())
                    .replace(ConfigPlaceholder.ITEM_STACK.placeholder, listingsDatabase.itemStack.type.toString())
                    .replace(ConfigPlaceholder.PRICE.placeholder, convertDoubleToReadableString(needingMoney))

            sendBukkitCommands(commandReplace)
        }
    }

    fun listingsToMailsNotification(it: Listings) {
        marketPlace.conf.expiration.listingsToMailsNotifCommand.forEach { command ->
            val commandReplace =
                command.replace(ConfigPlaceholder.PLAYER_PSEUDO.placeholder, it.sellerPseudo)
                    .replace(ConfigPlaceholder.PLAYER_UUID.placeholder, it.sellerUuid.toString())
                    .replace(ConfigPlaceholder.QUANTITY.placeholder, it.quantity.toString())
                    .replace(ConfigPlaceholder.ITEM_STACK.placeholder, it.itemStack.type.toString())
                    .replace(ConfigPlaceholder.PRICE.placeholder, convertDoubleToReadableString(it.price))

            sendBukkitCommands(commandReplace)
        }
    }

    fun mailsToDeleteNotification(it: Mails) {
        marketPlace.conf.expiration.mailsToDeleteNotifCommand.forEach { command ->
            val commandReplace =
                command.replace(
                    ConfigPlaceholder.PLAYER_PSEUDO.placeholder,
                    Bukkit.getOfflinePlayer(it.playerUuid).name ?: ""
                )
                    .replace(ConfigPlaceholder.PLAYER_UUID.placeholder, it.playerUuid.toString())
                    .replace(ConfigPlaceholder.QUANTITY.placeholder, it.quantity.toString())
                    .replace(ConfigPlaceholder.ITEM_STACK.placeholder, it.itemStack.type.toString())

            sendBukkitCommands(commandReplace)
        }
    }

    private fun sendBukkitCommands(commandReplace: String) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
        } else {
            Bukkit.getScheduler().runTaskLater(marketPlace.loader, Runnable {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
            }, 20)
        }
    }

}