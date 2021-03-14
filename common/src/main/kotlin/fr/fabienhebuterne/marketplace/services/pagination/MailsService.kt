package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.storage.MailsRepository

class MailsService(private val marketPlace: MarketPlace,
                   private val mailsRepository: MailsRepository) : PaginationService<Mails>(mailsRepository) {

    fun saveListingsToMail(listings: Listings) {
        val mails = mailsRepository.find(listings.sellerUuid, listings.itemStack)

        if (mails == null) {
            val mailCreation = Mails(
                    playerUuid = listings.sellerUuid,
                    playerPseudo = listings.sellerPseudo,
                    itemStack = listings.itemStack,
                    quantity = listings.quantity,
                    auditData = AuditData(
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            expiredAt = System.currentTimeMillis() + (marketPlace.configService.getSerialization().expiration.listingsToMails * 1000)
                    ),
                    version = marketPlace.itemStackReflection.getVersion()
            )
            mailsRepository.create(mailCreation)
        } else {
            val mailUpdate = mails.copy(
                    quantity = mails.quantity + listings.quantity,
                    auditData = mails.auditData.copy(
                            updatedAt = System.currentTimeMillis(),
                            expiredAt = System.currentTimeMillis() + (marketPlace.configService.getSerialization().expiration.listingsToMails * 1000)
                    )
            )
            mailsRepository.update(mailUpdate)
        }
    }

}
