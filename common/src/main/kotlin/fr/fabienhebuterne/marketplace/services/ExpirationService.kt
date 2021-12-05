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

    // We use only one async task to avoid conflict
    fun startTaskExpiration() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace.loader, Runnable {
            listingsToMails()
            mailsToDelete()
        }, 20 * 60, 20 * 600)
    }

    private fun listingsToMails() {
        val findAllListings = listingsService.findAll()
        findAllListings.forEach {
            if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                notificationService.listingsToMailsNotification(it)
                logsService.expirationListingsToMailsLog(it)
                it.id?.let { id -> listingsService.delete(id) }
                mailsService.saveListingsToMail(it)
            }
        }
    }

    private fun mailsToDelete() {
        val findAllMails = mailsService.findAll()
        findAllMails.forEach {
            if (it.auditData.expiredAt != null && it.auditData.expiredAt < System.currentTimeMillis()) {
                notificationService.mailsToDeleteNotification(it)
                logsService.expirationMailsToDeleteLog(it)
                it.id?.let { id -> mailsService.delete(id) }
            }
        }
    }

}
