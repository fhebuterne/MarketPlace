package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.COLLATION
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Filter
import fr.fabienhebuterne.marketplace.domain.base.FilterName
import fr.fabienhebuterne.marketplace.domain.base.FilterType
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.json.ItemStackSerializer
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.createdAt
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.expiredAt
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.id
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.itemStack
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.price
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.quantity
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.sellerPseudo
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.sellerUuid
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.updatedAt
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.version
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.world
import kotlinx.serialization.json.Json
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ListingsTable : UUIDTable("marketplace_listings") {
    val sellerUuid = ListingsTable.varchar("seller_uuid", 36, COLLATION)
    val sellerPseudo = ListingsTable.varchar("seller_pseudo", 16, COLLATION)
    val itemStack = ListingsTable.text("item_stack", COLLATION)
    val quantity = ListingsTable.integer("quantity")
    val price = ListingsTable.double("price")
    val world = ListingsTable.varchar("world", 200, COLLATION)
    val createdAt = ListingsTable.long("created_at")
    val updatedAt = ListingsTable.long("updated_at")
    val expiredAt = ListingsTable.long("expired_at")
    val version = ListingsTable.integer("version")
}

class ListingsRepositoryImpl(
    private val instance: MarketPlace,
    private val marketPlaceDb: Database
) : ListingsRepository {
    private val json = Json

    override fun fromRow(row: ResultRow): Listings {
        val itemStack: ItemStack = json.decodeFromString(ItemStackSerializer(instance, row[version]), row[itemStack])

        return Listings(
            id = row[id].value,
            sellerUuid = UUID.fromString(row[sellerUuid]),
            sellerPseudo = row[sellerPseudo],
            itemStack = itemStack,
            quantity = row[quantity],
            price = row[price],
            world = row[world],
            auditData = AuditData(
                createdAt = row[createdAt],
                updatedAt = row[updatedAt],
                expiredAt = row[expiredAt]
            ),
            version = row[version]
        )
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Listings): UpdateBuilder<Number> {
        val itemStackString = json.encodeToString(ItemStackSerializer(instance), entity.itemStack)

        entity.id?.let { insertTo[id] = EntityID(it, ListingsTable) }
        insertTo[sellerUuid] = entity.sellerUuid.toString()
        insertTo[sellerPseudo] = entity.sellerPseudo
        insertTo[itemStack] = itemStackString
        insertTo[quantity] = entity.quantity
        insertTo[price] = entity.price
        insertTo[world] = entity.world
        insertTo[createdAt] = entity.auditData.createdAt
        if (entity.auditData.updatedAt != null) {
            insertTo[updatedAt] = entity.auditData.updatedAt
        }
        if (entity.auditData.expiredAt != null) {
            insertTo[expiredAt] = entity.auditData.expiredAt
        }
        insertTo[version] = instance.itemStackReflection.getVersion()
        return insertTo
    }

    private fun filterDomainToStorage(filter: Filter): Pair<Column<*>, SortOrder> {
        val filterNameConverted = when (filter.filterName) {
            FilterName.CREATED_AT -> createdAt
            FilterName.EXPIRED_AT -> expiredAt
            FilterName.PRICE -> price
        }

        val filterTypeConverted = when (filter.filterType) {
            FilterType.ASC -> SortOrder.ASC
            FilterType.DESC -> SortOrder.DESC
        }

        return Pair(filterNameConverted, filterTypeConverted)
    }

    override fun findAll(uuid: UUID?, from: Int?, to: Int?, searchKeyword: String?, filter: Filter): List<Listings> {
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

    override fun findByLowerVersion(version: Int): List<Listings> {
        return transaction(marketPlaceDb) {
            ListingsTable
                .select { ListingsTable.version less version }
                .map { fromRow(it) }
        }
    }

    override fun find(id: String): Listings? {
        return transaction(marketPlaceDb) {
            ListingsTable
                .select { ListingsTable.id eq UUID.fromString(id) }
                .limit(1)
                .map { fromRow(it) }
                .firstOrNull()
        }
    }

    override fun find(sellerUuid: UUID, itemStack: ItemStack, price: Double): Listings? {
        val itemStackString = instance.itemStackReflection.serializeItemStack(itemStack)

        return transaction(marketPlaceDb) {
            ListingsTable.select {
                (ListingsTable.sellerUuid eq sellerUuid.toString()) and
                        (ListingsTable.itemStack eq itemStackString) and
                        (ListingsTable.price eq price)
            }.map { fromRow(it) }.firstOrNull()
        }
    }

    override fun findByUUID(sellerUuid: UUID): List<Listings> {
        return transaction(marketPlaceDb) {
            ListingsTable.select {
                ListingsTable.sellerUuid eq sellerUuid.toString()
            }.map { fromRow(it) }
        }
    }

    override fun findUUIDBySellerPseudo(sellerPseudo: String): UUID? {
        return transaction(marketPlaceDb) {
            ListingsTable.select {
                ListingsTable.sellerPseudo eq sellerPseudo
            }.limit(1).map { fromRow(it) }.firstOrNull()?.sellerUuid
        }
    }

    override fun create(entity: Listings): Listings {
        transaction(marketPlaceDb) {
            ListingsTable.insert { fromEntity(it, entity) }
        }
        return entity
    }

    override fun update(entity: Listings): Listings {
        transaction(marketPlaceDb) {
            ListingsTable.update({
                ListingsTable.id eq entity.id
            }) {
                fromEntity(it, entity)
            }
        }
        return entity
    }

    override fun delete(id: UUID) {
        transaction(marketPlaceDb) {
            ListingsTable.deleteWhere { ListingsTable.id eq id }
        }
    }

    override fun countAll(uuid: UUID?, searchKeyword: String?): Int {
        return transaction(marketPlaceDb) {
            buildSelect(uuid, searchKeyword).count().toInt()
        }
    }

    private fun buildSelect(uuid: UUID?, searchKeyword: String?): Query {
        return if (uuid == null) {
            if (searchKeyword == null) {
                ListingsTable.selectAll()
            } else {
                ListingsTable.select {
                    itemStack like "%$searchKeyword%"
                }
            }
        } else {
            if (searchKeyword == null) {
                ListingsTable.select {
                    sellerUuid eq uuid.toString()
                }
            } else {
                ListingsTable.select {
                    sellerUuid eq uuid.toString() and (itemStack like "%$searchKeyword%")
                }
            }
        }
    }
}
