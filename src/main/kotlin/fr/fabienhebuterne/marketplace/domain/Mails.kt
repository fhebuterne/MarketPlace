package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Mails(
        val id: UUID,
        val playerUuid: String,
        val itemStack: ItemStack,
        val quantity: Int = 1,
        val auditData: AuditData
)