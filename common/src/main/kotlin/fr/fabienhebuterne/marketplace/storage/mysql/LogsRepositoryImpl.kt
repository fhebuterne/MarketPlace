package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.COLLATION
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Filter
import fr.fabienhebuterne.marketplace.domain.base.FilterName
import fr.fabienhebuterne.marketplace.domain.base.FilterType
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.json.ItemStackSerializer
import fr.fabienhebuterne.marketplace.storage.LogsRepository
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.adminPseudo
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.adminUuid
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.createdAt
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.fromLocation
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.id
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.itemStack
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.logType
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.playerPseudo
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.playerUuid
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.price
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.quantity
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.sellerPseudo
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.sellerUuid
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.toLocation
import fr.fabienhebuterne.marketplace.storage.mysql.LogsTable.version
import kotlinx.serialization.json.Json
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object LogsTable : UUIDTable("marketplace_logs") {
    val playerUuid = LogsTable.varchar("player_uuid", 36, COLLATION).nullable()
    val playerPseudo = LogsTable.varchar("player_pseudo", 16, COLLATION)
    val sellerUuid = LogsTable.varchar("seller_uuid", 36, COLLATION).nullable()
    val sellerPseudo = LogsTable.varchar("seller_pseudo", 16, COLLATION).nullable()
    val adminUuid = LogsTable.varchar("admin_uuid", 36, COLLATION).nullable()
    val adminPseudo = LogsTable.varchar("admin_pseudo", 16, COLLATION).nullable()
    val itemStack = LogsTable.text("item_stack", COLLATION)
    val quantity = LogsTable.integer("quantity")
    val price = LogsTable.double("price").nullable()
    val logType = LogsTable.enumerationByName("log_type", 100, LogType::class)
    val fromLocation = LogsTable.enumerationByName("from_location", 100, Location::class)
    val toLocation = LogsTable.enumerationByName("to_location", 100, Location::class)
    val createdAt = LogsTable.long("created_at")
    val version = LogsTable.integer("version")
}

class LogsRepositoryImpl(
    private val instance: MarketPlace,
    private val marketPlaceDb: Database
) : LogsRepository {
    private val json = Json

    override fun fromRow(row: ResultRow): Logs {
        val itemStack: ItemStack = json.decodeFromString(ItemStackSerializer(instance, row[version]), row[itemStack])

        return Logs(
            id = row[id].value,
            playerUuid = UUID.fromString(row[playerUuid]),
            playerPseudo = row[playerPseudo],
            sellerUuid = row[sellerUuid]?.let { UUID.fromString(row[sellerUuid]) },
            sellerPseudo = row[sellerPseudo],
            adminUuid = row[adminUuid]?.let { UUID.fromString(row[adminUuid]) },
            adminPseudo = row[adminPseudo],
            itemStack = itemStack,
            quantity = row[quantity],
            price = row[price],
            logType = row[logType],
            toLocation = row[toLocation],
            fromLocation = row[fromLocation],
            auditData = AuditData(
                createdAt = row[createdAt]
            ),
            version = row[version]
        )
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Logs): UpdateBuilder<Number> {
        entity.id?.let { insertTo[id] = EntityID(it, LogsTable) }
        entity.price?.let { insertTo[price] = it }
        entity.sellerUuid?.let { insertTo[sellerUuid] = it.toString() }
        entity.sellerPseudo?.let { insertTo[sellerPseudo] = it }
        entity.adminUuid?.let { insertTo[adminUuid] = it.toString() }
        entity.adminPseudo?.let { insertTo[adminPseudo] = it }

        val itemStackString = json.encodeToString(ItemStackSerializer(instance), entity.itemStack)
        insertTo[itemStack] = itemStackString
        insertTo[playerUuid] = entity.playerUuid.toString()
        insertTo[playerPseudo] = entity.playerPseudo
        insertTo[quantity] = entity.quantity
        insertTo[logType] = entity.logType
        insertTo[toLocation] = entity.toLocation
        insertTo[fromLocation] = entity.fromLocation
        insertTo[createdAt] = entity.auditData.createdAt
        insertTo[version] = instance.itemStackReflection.getVersion()
        return insertTo
    }

    private fun filterDomainToStorage(filter: Filter): Pair<Column<*>, SortOrder> {
        val filterNameConverted = when (filter.filterName) {
            FilterName.PRICE -> price
            else -> createdAt
        }

        val filterTypeConverted = when (filter.filterType) {
            FilterType.ASC -> SortOrder.ASC
            FilterType.DESC -> SortOrder.DESC
        }

        return Pair(filterNameConverted, filterTypeConverted)
    }

    override fun findAll(uuid: UUID?, from: Int?, to: Int?, searchKeyword: String?, filter: Filter): List<Logs> {
        return transaction(marketPlaceDb) {
            val selectBase = buildSelect(uuid, searchKeyword)

            val find = if (from != null && to != null) {
                selectBase.limit(to, from.toLong())
            } else {
                selectBase
            }

            find.orderBy(filterDomainToStorage(filter))
                .map { fromRow(it) }
        }
    }

    override fun findByLowerVersion(version: Int): List<Logs> {
        return transaction(marketPlaceDb) {
            LogsTable
                .select { LogsTable.version less version }
                .map { fromRow(it) }
        }
    }

    override fun find(id: String): Logs? {
        return transaction(marketPlaceDb) {
            LogsTable
                .select { LogsTable.id eq UUID.fromString(id) }
                .limit(1)
                .map { fromRow(it) }
                .firstOrNull()
        }
    }

    override fun findByUUID(playerUuid: UUID): List<Logs> {
        return transaction(marketPlaceDb) {
            LogsTable.select {
                LogsTable.playerUuid eq playerUuid.toString()
            }.map { fromRow(it) }
        }
    }

    override fun create(entity: Logs): Logs {
        transaction(marketPlaceDb) {
            LogsTable.insert { fromEntity(it, entity) }
        }
        return entity
    }

    override fun update(entity: Logs): Logs {
        transaction(marketPlaceDb) {
            LogsTable.update({
                LogsTable.id eq entity.id
            }) {
                fromEntity(it, entity)
            }
        }
        return entity
    }

    override fun delete(id: UUID) {
        TODO("Not yet implemented")
    }

    override fun countAll(uuid: UUID?, searchKeyword: String?): Int {
        return transaction(marketPlaceDb) {
            buildSelect(uuid, searchKeyword).count().toInt()
        }
    }

    override fun findUUIDByPseudo(playerPseudo: String): UUID? {
        val log = transaction(marketPlaceDb) {
            LogsTable.select {
                (LogsTable.playerPseudo eq playerPseudo) or
                        (LogsTable.sellerPseudo eq playerPseudo) or
                        (LogsTable.adminPseudo eq playerPseudo)
            }.limit(1).map { fromRow(it) }.firstOrNull()
        } ?: return null

        return when {
            log.adminPseudo == playerPseudo -> log.adminUuid
            log.playerPseudo == playerPseudo -> log.playerUuid
            log.sellerPseudo == playerPseudo -> log.sellerUuid
            else -> null
        }
    }

    private fun buildSelect(uuid: UUID?, searchKeyword: String?): Query {
        return if (uuid == null) {
            if (searchKeyword == null) {
                LogsTable.selectAll()
            } else {
                LogsTable.select {
                    itemStack like "%$searchKeyword%"
                }
            }
        } else {
            if (searchKeyword == null) {
                LogsTable.select {
                    (sellerUuid eq uuid.toString()) or
                            (playerUuid eq uuid.toString()) or
                            (adminUuid eq uuid.toString())
                }
            } else {
                LogsTable.select {
                    sellerUuid eq uuid.toString() and (itemStack like "%$searchKeyword%")
                }
            }
        }
    }
}
