package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.config.DefaultConfigService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

class ExpirationService(
    private val marketPlace: MarketPlace,
    private val listingsService: ListingsService,
    private val mailsService: MailsService,
    private val logsService: LogsService,
    private val notificationService: NotificationService,
    private val defaultConfigService: DefaultConfigService
) {
    private lateinit var currentTask: BukkitTask

    // We use only one async task to avoid conflict
    fun startTaskExpiration() {
        if (defaultConfigService.getSerialization().expiration.allExpirationsDisabled) {
            marketPlace.loader.logger.info("Expiration system is fully disabled")
            return
        }
        marketPlace.loader.logger.info("Start expiration task")
        currentTask = Bukkit.getScheduler().runTaskTimerAsynchronously(marketPlace.loader, Runnable {
            if (defaultConfigService.getSerialization().expiration.listingsToMailsEnabled) {
                listingsToMails()
            }
            if (defaultConfigService.getSerialization().expiration.mailsToDeleteEnabled) {
                mailsToDelete()
            }
        }, 20 * 60, 20 * 600)
    }

    fun stopTaskExpiration() {
        if (!isTaskCancelled()) {
            marketPlace.loader.logger.info("Stop expiration task")
            currentTask.cancel()
        }
    }

    fun isTaskCancelled(): Boolean {
        return currentTask.isCancelled
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
