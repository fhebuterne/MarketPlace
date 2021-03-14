package fr.fabienhebuterne.marketplace.domain.paginated

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import org.bukkit.inventory.ItemStack
import java.util.*

data class Logs(
    override val id: UUID? = null,
    val playerUuid: UUID,
    val playerPseudo: String,
    val sellerUuid: UUID? = null,
    val sellerPseudo: String? = null,
    val adminUuid: UUID? = null,
    val adminPseudo: String? = null,
    override val itemStack: ItemStack,
    val quantity: Int = 1,
    val price: Double? = null,
    val logType: LogType,
    val fromLocation: Location,
    val toLocation: Location,
    val auditData: AuditData,
    override val version: Int
) : Entity(id, itemStack, version)

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
