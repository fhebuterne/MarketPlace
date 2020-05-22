package fr.fabienhebuterne.marketplace.domain.paginated

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Listings(
        val id: UUID? = null,
        val sellerUuid: UUID,
        val sellerPseudo: String,
        val itemStack: ItemStack,
        val quantity: Int = 1,
        val price: Long,
        val world: String,
        val auditData: AuditData
) : Paginated