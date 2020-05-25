package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.json.ITEMSTACK_MODULE
import fr.fabienhebuterne.marketplace.json.ItemStackSerializer
import fr.fabienhebuterne.marketplace.storage.LogsRepository
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object LogsTable : UUIDTable("marketplace_logs") {
    val playerUuid = LogsTable.varchar("player_uuid", 36)
    val playerPseudo = LogsTable.varchar("player_pseudo", 16)
    val sellerUuid = LogsTable.varchar("seller_uuid", 36).nullable()
    val sellerPseudo = LogsTable.varchar("seller_pseudo", 16).nullable()
    val itemStack = LogsTable.text("item_stack")
    val quantity = LogsTable.integer("quantity")
    val price = LogsTable.long("price").nullable()
    val logType = LogsTable.enumerationByName("log_type", 100, LogType::class)
    val fromLocation = LogsTable.enumerationByName("from_location", 100, Location::class)
    val toLocation = LogsTable.enumerationByName("to_location", 100, Location::class)
    val createdAt = LogsTable.long("created_at")
}

class LogsRepositoryImpl(private val marketPlaceDb: Database) : LogsRepository {
    private val json = Json(JsonConfiguration.Stable, context = ITEMSTACK_MODULE)

    override fun fromRow(row: ResultRow): Logs {
        val itemStack: ItemStack = json.parse(ItemStackSerializer, row[itemStack])

        return Logs(
                id = row[id].value,
                playerUuid = UUID.fromString(row[playerUuid]),
                playerPseudo = row[playerPseudo],
                sellerUuid = UUID.fromString(row[sellerUuid]),
                sellerPseudo = row[sellerPseudo],
                itemStack = itemStack,
                quantity = row[quantity],
                price = row[price],
                logType = row[logType],
                toLocation = row[toLocation],
                fromLocation = row[fromLocation],
                auditData = AuditData(
                        createdAt = row[createdAt]
                )
        )
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Logs): UpdateBuilder<Number> {
        if (entity.itemStack != null) {
            val itemStackString = json.stringify(ItemStackSerializer, entity.itemStack)
            insertTo[itemStack] = itemStackString
        }

        entity.id?.let { insertTo[id] = EntityID(it, LogsTable) }
        entity.price?.let { insertTo[price] = it }
        entity.sellerUuid?.let { insertTo[sellerUuid] = it.toString() }
        entity.sellerPseudo?.let {  insertTo[sellerPseudo] = it }

        insertTo[playerUuid] = entity.playerUuid.toString()
        insertTo[playerPseudo] = entity.playerPseudo
        insertTo[quantity] = entity.quantity
        insertTo[logType] = entity.logType
        insertTo[toLocation] = entity.toLocation
        insertTo[fromLocation] = entity.fromLocation
        insertTo[createdAt] = entity.auditData.createdAt
        return insertTo
    }

    override fun findAll(from: Int?, to: Int?, searchKeyword: String?): List<Logs> {
        return transaction(marketPlaceDb) {
            when (from != null && to != null) {
                true -> LogsTable.selectAll().limit(to, from.toLong()).map { fromRow(it) }
                false -> LogsTable.selectAll().map { fromRow(it) }
            }
        }
    }

    override fun find(id: String): Logs? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID) {
        TODO("Not yet implemented")
    }

    override fun countAll(searchKeyword: String?): Int {
        return transaction(marketPlaceDb) {
            when (searchKeyword == null) {
                true -> LogsTable.selectAll().count().toInt()
                false -> LogsTable.select { itemStack like "%$searchKeyword%" }.count().toInt()
            }
        }
    }
}