package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Filter
import fr.fabienhebuterne.marketplace.domain.base.FilterName
import fr.fabienhebuterne.marketplace.domain.base.FilterType
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.json.ItemStackSerializer
import fr.fabienhebuterne.marketplace.storage.MailsRepository
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.createdAt
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.expiredAt
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.id
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.itemStack
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.playerPseudo
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.playerUuid
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.quantity
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.updatedAt
import fr.fabienhebuterne.marketplace.storage.mysql.MailsTable.version
import kotlinx.serialization.json.Json
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object MailsTable : UUIDTable("marketplace_mails") {
    val playerUuid = MailsTable.varchar("player_uuid", 36)
    val playerPseudo = MailsTable.varchar("player_pseudo", 16)
    val itemStack = MailsTable.text("item_stack")
    val quantity = MailsTable.integer("quantity")
    val createdAt = MailsTable.long("created_at")
    val updatedAt = MailsTable.long("updated_at")
    val expiredAt = MailsTable.long("expired_at")
    val version = MailsTable.integer("version")
}

class MailsRepositoryImpl(
    private val instance: MarketPlace,
    private val marketPlaceDb: Database
) : MailsRepository {
    private val json = Json

    override fun fromRow(row: ResultRow): Mails {
        val itemStack: ItemStack = json.decodeFromString(ItemStackSerializer(instance), row[itemStack])

        return Mails(
            id = row[id].value,
            playerUuid = UUID.fromString(row[playerUuid]),
            playerPseudo = row[playerPseudo],
            itemStack = itemStack,
            quantity = row[quantity],
            auditData = AuditData(
                createdAt = row[createdAt],
                updatedAt = row[updatedAt],
                expiredAt = row[expiredAt]
            ),
            version = instance.itemStackReflection.getVersion()
        )
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Mails): UpdateBuilder<Number> {
        val itemStackString = json.encodeToString(ItemStackSerializer(instance, entity.version), entity.itemStack)

        entity.id?.let { insertTo[id] = EntityID(it, MailsTable) }
        entity.auditData.updatedAt?.let { insertTo[updatedAt] = it }
        entity.auditData.expiredAt?.let { insertTo[expiredAt] = it }

        insertTo[playerUuid] = entity.playerUuid.toString()
        insertTo[playerPseudo] = entity.playerPseudo
        insertTo[itemStack] = itemStackString
        insertTo[quantity] = entity.quantity
        insertTo[createdAt] = entity.auditData.createdAt
        insertTo[version] = entity.version
        return insertTo
    }

    private fun filterDomainToStorage(filter: Filter): Pair<Column<*>, SortOrder> {
        val filterNameConverted = when (filter.filterName) {
            FilterName.CREATED_AT -> createdAt
            FilterName.EXPIRED_AT -> expiredAt
            else -> createdAt
        }

        val filterTypeConverted = when (filter.filterType) {
            FilterType.ASC -> SortOrder.ASC
            FilterType.DESC -> SortOrder.DESC
        }

        return Pair(filterNameConverted, filterTypeConverted)
    }

    override fun findAll(uuid: UUID?, from: Int?, to: Int?, searchKeyword: String?, filter: Filter): List<Mails> {
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

    override fun findByLowerVersion(version: Int): List<Mails> {
        return transaction(marketPlaceDb) {
            MailsTable
                .select { MailsTable.version less version }
                .map { fromRow(it) }
        }
    }

    override fun find(id: String): Mails? {
        TODO("Not yet implemented")
    }

    override fun find(playerUuid: UUID, itemStack: ItemStack): Mails? {
        val itemStackString = json.encodeToString(ItemStackSerializer(instance), itemStack)

        return transaction(marketPlaceDb) {
            MailsTable.select {
                (MailsTable.playerUuid eq playerUuid.toString()) and
                        (MailsTable.itemStack eq itemStackString)
            }.map { fromRow(it) }.firstOrNull()
        }
    }

    override fun findByUUID(playerUuid: UUID): List<Mails> {
        return transaction(marketPlaceDb) {
            MailsTable.select {
                MailsTable.playerUuid eq playerUuid.toString()
            }.map { fromRow(it) }
        }
    }

    override fun create(entity: Mails): Mails {
        transaction(marketPlaceDb) {
            MailsTable.insert { fromEntity(it, entity) }
        }
        return entity
    }

    override fun update(entity: Mails): Mails {
        transaction(marketPlaceDb) {
            MailsTable.update({
                MailsTable.id eq entity.id
            }) {
                fromEntity(it, entity)
            }
        }
        return entity
    }

    override fun delete(id: UUID) {
        transaction(marketPlaceDb) {
            MailsTable.deleteWhere { MailsTable.id eq id }
        }
    }

    override fun countAll(uuid: UUID?, searchKeyword: String?): Int {
        return transaction(marketPlaceDb) {
            buildSelect(uuid, searchKeyword).count().toInt()
        }
    }

    private fun buildSelect(uuid: UUID?, searchKeyword: String?): Query {
        return if (uuid == null) {
            MailsTable.selectAll()
        } else {
            if (searchKeyword == null) {
                MailsTable.select {
                    playerUuid eq uuid.toString()
                }
            } else {
                MailsTable.select {
                    playerUuid eq uuid.toString() and (itemStack like "%$searchKeyword%")
                }
            }
        }
    }

    override fun findUuidByPseudo(playerPseudo: String): UUID? {
        val transaction = transaction(marketPlaceDb) {
            MailsTable.select {
                MailsTable.playerPseudo eq playerPseudo
            }.limit(1).firstOrNull()
        }

        return transaction?.let {
            fromRow(it).playerUuid
        }
    }
}
