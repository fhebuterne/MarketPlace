package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import java.util.*

interface LogsRepository : PaginationRepository<Logs> {
    fun findByUUID(playerUuid: UUID): List<Logs>
}