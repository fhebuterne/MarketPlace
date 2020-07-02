package fr.fabienhebuterne.marketplace.domain.paginated

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Mails(
        val id: UUID? = null,
        val playerPseudo: String,
        val playerUuid: UUID,
        val itemStack: ItemStack,
        val quantity: Int = 1,
        val auditData: AuditData
) : Paginated