package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.json.ITEMSTACK_MODULE
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
import fr.fabienhebuterne.marketplace.storage.mysql.ListingsTable.world
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ListingsTable : UUIDTable("marketplace_listings") {
    val sellerUuid = ListingsTable.varchar("seller_uuid", 36)
    val sellerPseudo = ListingsTable.varchar("seller_pseudo", 16)
    val itemStack = ListingsTable.text("item_stack")
    val quantity = ListingsTable.integer("quantity")
    val price = ListingsTable.long("price")
    val world = ListingsTable.varchar("world", 200)
    val createdAt = ListingsTable.long("created_at")
    val updatedAt = ListingsTable.long("updated_at")
    val expiredAt = ListingsTable.long("expired_at")
}

class ListingsRepositoryImpl(private val marketPlaceDb: Database) : ListingsRepository {
    private val json = Json(JsonConfiguration.Stable, context = ITEMSTACK_MODULE)

    override fun fromRow(row: ResultRow): Listings {
        val itemStack: ItemStack = json.parse(ItemStackSerializer, row[itemStack])

        return Listings(
                id = row[id].value,
                sellerUuid = row[sellerUuid],
                sellerPseudo = row[sellerPseudo],
                itemStack = itemStack,
                quantity = row[quantity],
                price = row[price],
                world = row[world],
                auditData = AuditData(
                        createdAt = row[createdAt],
                        updatedAt = row[updatedAt],
                        expiredAt = row[expiredAt]
                )
        )
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Listings): UpdateBuilder<Number> {
        val itemStackString = json.stringify(ItemStackSerializer, entity.itemStack)

        insertTo[id] = EntityID(entity.id, ListingsTable)
        insertTo[sellerUuid] = entity.sellerUuid
        insertTo[sellerPseudo] = entity.sellerPseudo
        insertTo[itemStack] = itemStackString
        insertTo[quantity] = entity.quantity
        insertTo[price] = entity.price
        insertTo[world] = entity.world
        insertTo[createdAt] = entity.auditData.createdAt
        insertTo[updatedAt] = entity.auditData.updatedAt
        insertTo[expiredAt] = entity.auditData.expiredAt
        return insertTo
    }

    override fun findAll(from: Int?, to: Int?): List<Listings> {
        return transaction(marketPlaceDb) {
            when (from != null && to != null) {
                true -> ListingsTable.selectAll().limit(to, from.toLong()).map { fromRow(it) }
                false -> ListingsTable.selectAll().map { fromRow(it) }
            }
        }
    }

    override fun find(id: String): Listings? {
        TODO("Not yet implemented")
    }

    override fun find(sellerUuid: String, itemStack: ItemStack, price: Long): Listings? {
        val itemStackString = json.stringify(ItemStackSerializer, itemStack)

        return transaction(marketPlaceDb) {
            ListingsTable.select {
                (ListingsTable.sellerUuid eq sellerUuid) and
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

    override fun create(entity: Listings): Listings {
        transaction(marketPlaceDb) {
            ListingsTable.insert { fromEntity(it, entity) }
        }
        return entity
    }

    override fun update(entity: Listings): Listings {
        val itemStackString = json.stringify(ItemStackSerializer, entity.itemStack)

        transaction(marketPlaceDb) {
            ListingsTable.update({
                (sellerUuid eq entity.sellerUuid) and
                        (itemStack eq itemStackString) and
                        (price eq entity.price)
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

    override fun countAll(): Int {
        return transaction(marketPlaceDb) {
            ListingsTable.selectAll().count().toInt()
        }
    }
}