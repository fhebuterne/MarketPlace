package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.storage.MailsRepository

class MailsService(private val mailsRepository: MailsRepository) : PaginationService<Mails>(mailsRepository) {

    fun saveListingsToMail(listings: Listings) {
        val mails = mailsRepository.find(listings.sellerUuid.toString(), listings.itemStack)

        // TODO : Add audit log here

        if (mails == null) {
            val mailCreation = Mails(
                    playerUuid = listings.sellerUuid.toString(),
                    itemStack = listings.itemStack,
                    quantity = listings.quantity,
                    auditData = AuditData(
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            expiredAt = System.currentTimeMillis() + (3600 * 24 * 7 * 1000)
                    )
            )
            mailsRepository.create(mailCreation)
        } else {
            val mailUpdate = mails.copy(
                    quantity = mails.quantity + listings.quantity,
                    auditData = mails.auditData.copy(
                            updatedAt = System.currentTimeMillis(),
                            expiredAt = System.currentTimeMillis() + (3600 * 24 * 7 * 1000)
                    )
            )
            mailsRepository.update(mailUpdate)
        }
    }

}