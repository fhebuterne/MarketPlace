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

    // TODO : Put time in config.json
    fun startTaskExpirationListingsToMails() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace, {
            val findAllListings = listingsService.findAll()
            findAllListings.forEach {
                if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                    // TODO : Execute notif command
                    logsService.createFrom(
                            Bukkit.getOfflinePlayer(it.sellerUuid).player,
                            it,
                            it.quantity,
                            null,
                            LogType.EXPIRED,
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace, {
            val findAllMails = mailsService.findAll()
            findAllMails.forEach {
                if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                    // TODO : Execute notif command
                    logsService.createFrom(
                            Bukkit.getOfflinePlayer(it.playerUuid).player,
                            it,
                            it.quantity,
                            null,
                            LogType.EXPIRED,
                            fromLocation = Location.MAIL_INVENTORY,
                            toLocation = Location.NONE
                    )

                    it.id?.let { id -> mailsService.delete(id) }
                }
            }
        }, 20 * 60, 20 * 600)
    }

}