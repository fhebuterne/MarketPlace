package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.Bukkit

class ExpirationService(
    private val marketPlace: MarketPlace,
    private val listingsService: ListingsService,
    private val mailsService: MailsService,
    private val logsService: LogsService,
    private val notificationService: NotificationService
) {

    fun startTaskExpirationListingsToMails() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace.loader, Runnable {
            val findAllListings = listingsService.findAll()
            findAllListings.forEach {
                if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                    notificationService.listingsToMailsNotification(it)
                    logsService.expirationListingsToMailsLog(it)
                    it.id?.let { id -> listingsService.delete(id) }
                    mailsService.saveListingsToMail(it)
                }
            }
        }, 20 * 60, 20 * 600)
    }

    fun startTaskExpirationMailsToDelete() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace.loader, Runnable {
            val findAllMails = mailsService.findAll()
            findAllMails.forEach {
                if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                    notificationService.mailsToDeleteNotification(it)
                    logsService.expirationMailsToDeleteLog(it)
                    it.id?.let { id -> mailsService.delete(id) }
                }
            }
        }, 20 * 60, 20 * 600)
    }

}
