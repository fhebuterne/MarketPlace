package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService

/**
 * Class used to migrate all entities contains ItemStack
 * between Minecraft versions on server startup
 */
class MigrationService(
    private val marketPlace: MarketPlace,
    private val listingsService: ListingsService,
    private val mailsService: MailsService,
    private val logsService: LogsService
) {

    fun migrateAllEntities() {
        marketPlace.loader.logger.info("Migration to latest Minecraft version started")
        listingsService.migrateEntities()
        mailsService.migrateEntities()
        logsService.migrateEntities()
        marketPlace.loader.logger.info("Migration to latest Minecraft version ended")
    }

}
