package fr.fabienhebuterne.marketplace.domain.paginated

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Listings(
        override val id: UUID? = null,
        val sellerUuid: UUID,
        val sellerPseudo: String,
        override val itemStack: ItemStack,
        val quantity: Int = 1,
        val price: Double,
        val world: String,
        val auditData: AuditData,
        override val version: Int
) : Entity(id, itemStack, version)
