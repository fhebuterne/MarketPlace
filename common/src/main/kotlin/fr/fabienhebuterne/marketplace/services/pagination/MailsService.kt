package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import org.bukkit.entity.Player

class MailsService(
    private val marketPlace: MarketPlace,
    private val mailsRepository: MailsRepository
) : PaginationService<Mails>(mailsRepository, marketPlace) {

    fun saveListingsToMail(
        listing: Listings,
        player: Player? = null,
        quantity: Int? = null
    ) {
        val uuid = player?.uniqueId ?: listing.sellerUuid
        val pseudo = player?.name ?: listing.sellerPseudo
        val selectQuantity = quantity ?: listing.quantity

        val mailsDatabase = mailsRepository.find(
            player?.uniqueId ?: listing.sellerUuid,
            listing.itemStack
        )

        if (mailsDatabase == null) {
            val mailCreation = Mails(
                playerUuid = uuid,
                playerPseudo = pseudo,
                itemStack = listing.itemStack,
                quantity = selectQuantity,
                auditData = AuditData(
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    expiredAt = System.currentTimeMillis() + (marketPlace.conf.expiration.listingsToMails * 1000)
                ),
                version = marketPlace.itemStackReflection.getVersion()
            )
            mailsRepository.create(mailCreation)
        } else {
            val mailUpdate = mailsDatabase.copy(
                quantity = mailsDatabase.quantity + selectQuantity,
                auditData = mailsDatabase.auditData.copy(
                    updatedAt = System.currentTimeMillis(),
                    expiredAt = System.currentTimeMillis() + (marketPlace.conf.expiration.listingsToMails * 1000)
                )
            )
            mailsRepository.update(mailUpdate)
        }
    }

}
