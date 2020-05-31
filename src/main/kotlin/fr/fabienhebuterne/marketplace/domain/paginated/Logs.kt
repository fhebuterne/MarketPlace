package fr.fabienhebuterne.marketplace.domain.paginated

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Logs(
        val id: UUID? = null,
        val playerUuid: UUID,
        val playerPseudo: String,
        val sellerUuid: UUID? = null,
        val sellerPseudo: String? = null,
        val itemStack: ItemStack? = null,
        val quantity: Int = 1,
        val price: Long? = null,
        val logType: LogType,
        val fromLocation: Location,
        val toLocation: Location,
        val auditData: AuditData
) : Paginated

enum class LogType {
    SELL,
    BUY,
    EXPIRED,
    CANCEL,
    GET
}

enum class Location {
    PLAYER_INVENTORY,
    LISTING_INVENTORY,
    MAIL_INVENTORY,
    NONE
}