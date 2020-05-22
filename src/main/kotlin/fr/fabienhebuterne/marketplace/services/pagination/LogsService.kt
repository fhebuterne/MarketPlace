package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.*
import fr.fabienhebuterne.marketplace.storage.LogsRepository
import org.bukkit.entity.Player

class LogsService(private val logsRepository: LogsRepository) : PaginationService<Logs>(logsRepository) {

    fun createFrom(
            player: Player,
            paginated: Paginated,
            quantity: Int,
            needingMoney: Long?,
            logType: LogType,
            fromLocation: Location,
            toLocation: Location
    ) {
        var logs = Logs(
                playerUuid = player.uniqueId,
                playerPseudo = player.name,
                quantity = quantity,
                logType = logType,
                fromLocation = fromLocation,
                toLocation = toLocation,
                auditData = AuditData(
                        createdAt = System.currentTimeMillis()
                )
        )

        if (needingMoney != null) {
            logs = logs.copy(
                    price = needingMoney
            )
        }

        if (paginated is Listings) {
            logs = logs.copy(
                    sellerUuid = paginated.sellerUuid,
                    sellerPseudo = paginated.sellerPseudo,
                    itemStack = paginated.itemStack
            )
        }

        if (paginated is Mails) {
            logs = logs.copy(
                    itemStack = paginated.itemStack
            )
        }

        logsRepository.create(logs)
    }

}