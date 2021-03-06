package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import org.bukkit.inventory.ItemStack
import java.util.*

interface MailsRepository : PaginationRepository<Mails> {
    fun find(playerUuid: UUID, itemStack: ItemStack): Mails?
    fun findByUUID(playerUuid: UUID): List<Mails>
    fun findUuidByPseudo(playerPseudo: String): UUID?
}