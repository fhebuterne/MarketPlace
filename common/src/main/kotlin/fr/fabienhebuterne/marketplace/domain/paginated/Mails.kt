package fr.fabienhebuterne.marketplace.domain.paginated

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Mails(
    override val id: UUID? = null,
    val playerPseudo: String,
    val playerUuid: UUID,
    override val itemStack: ItemStack,
    val quantity: Int = 1,
    val auditData: AuditData,
    override val version: Int
) : Paginated(id, itemStack, version)
