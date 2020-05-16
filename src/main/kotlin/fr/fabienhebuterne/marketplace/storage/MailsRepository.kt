package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Mails
import java.util.*

interface MailsRepository : Repository<Mails> {
    fun findByUUID(playerUuid: UUID): List<Mails>
}