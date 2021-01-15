package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.Bukkit

class ExpirationService(
        private val marketPlace: MarketPlace,
        private val listingsService: ListingsService,
        private val mailsService: MailsService,
        private val logsService: LogsService
) {

    fun startTaskExpirationListingsToMails() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace, Runnable {
            val findAllListings = listingsService.findAll()
            findAllListings.forEach {
                if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                    marketPlace.configService.getSerialization().expiration.listingsToMailsNotifCommand.forEach { command ->
                        val commandReplace = command.replace("{{playerPseudo}}", it.sellerPseudo)
                            .replace("{{playerUUID}}", it.sellerUuid.toString())
                            .replace("{{quantity}}", it.quantity.toString())
                            .replace("{{itemStack}}", it.itemStack.type.toString())
                            .replace("{{price}}", it.price.toString())

                        if (Bukkit.isPrimaryThread()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
                        } else {
                            Bukkit.getScheduler().runTaskLater(marketPlace, Runnable {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
                            }, 20)
                        }
                    }

                    logsService.createFrom(
                            player = Bukkit.getOfflinePlayer(it.sellerUuid),
                            paginated = it,
                            quantity = it.quantity,
                            needingMoney = null,
                            logType = LogType.EXPIRED,
                            fromLocation = Location.LISTING_INVENTORY,
                            toLocation = Location.MAIL_INVENTORY
                    )

                    it.id?.let { id -> listingsService.delete(id) }
                    mailsService.saveListingsToMail(it)
                }
            }
        }, 20 * 60, 20 * 600)
    }

    fun startTaskExpirationMailsToDelete() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace, Runnable {
            val findAllMails = mailsService.findAll()
            findAllMails.forEach {
                if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                    marketPlace.configService.getSerialization().expiration.mailsToDeleteNotifCommand.forEach { command ->
                        val commandReplace =
                            command.replace("{{playerPseudo}}", Bukkit.getOfflinePlayer(it.playerUuid).name)
                                .replace("{{playerUUID}}", it.playerUuid.toString())
                                .replace("{{quantity}}", it.quantity.toString())
                                .replace("{{itemStack}}", it.itemStack.type.toString())

                        if (Bukkit.isPrimaryThread()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
                        } else {
                            Bukkit.getScheduler().runTaskLater(marketPlace, Runnable {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReplace)
                            }, 20)
                        }
                    }

                    logsService.createFrom(
                        player = Bukkit.getOfflinePlayer(it.playerUuid).player!!,
                        paginated = it,
                        quantity = it.quantity,
                        needingMoney = null,
                        logType = LogType.EXPIRED,
                        fromLocation = Location.MAIL_INVENTORY,
                        toLocation = Location.NONE
                    )

                    it.id?.let { id -> mailsService.delete(id) }
                }
            }
        }, 20 * 60, 20 * 600)
    }

}
