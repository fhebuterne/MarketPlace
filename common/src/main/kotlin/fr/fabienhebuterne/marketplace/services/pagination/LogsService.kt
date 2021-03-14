package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.*
import fr.fabienhebuterne.marketplace.storage.LogsRepository
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class LogsService(private val marketPlace: MarketPlace,
                  private val logsRepository: LogsRepository) : PaginationService<Logs>(logsRepository) {

    fun createFrom(
            player: OfflinePlayer,
            adminPlayer: Player? = null,
            paginated: Entity,
            quantity: Int,
            needingMoney: Double?,
            logType: LogType,
            fromLocation: Location,
            toLocation: Location
    ) {
        var logs = Logs(
            playerUuid = player.uniqueId,
            playerPseudo = player.name ?: "???",
            quantity = quantity,
            logType = logType,
            fromLocation = fromLocation,
            toLocation = toLocation,
            auditData = AuditData(
                createdAt = System.currentTimeMillis()
            ),
            itemStack = paginated.itemStack,
            version = marketPlace.itemStackReflection.getVersion()
        )

        if (adminPlayer != null) {
            logs = logs.copy(
                    adminUuid = adminPlayer.uniqueId,
                    adminPseudo = adminPlayer.name
            )
        }

        if (needingMoney != null) {
            logs = logs.copy(
                    price = needingMoney
            )
        }

        if (paginated is Listings) {
            logs = logs.copy(
                    sellerUuid = paginated.sellerUuid,
                    sellerPseudo = paginated.sellerPseudo
            )
        }

        logsRepository.create(logs)
    }

}
