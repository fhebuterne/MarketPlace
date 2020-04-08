package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Items
import fr.fabienhebuterne.marketplace.domain.ItemsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ItemsRepositoryImpl(private val marketPlaceDb: Database) : ItemsRepository {
    override fun fromRow(row: ResultRow) = Items(
            id = row[ItemsTable.id].value,
            item = row[ItemsTable.item] as MutableMap<String, Any>
    )

    override fun fromEntity(insertTo: InsertStatement<Number>, entity: Items): InsertStatement<Number> {
        insertTo[ItemsTable.id] = EntityID(entity.id, ItemsTable)
        insertTo[ItemsTable.item] = entity.item.toString()
        return insertTo
    }

    override fun findAll(): List<Items> {
        TODO("Not yet implemented")
    }

    override fun find(id: String): Items? {
        return transaction(marketPlaceDb) {
            ItemsTable.select(Op.build { ItemsTable.id eq UUID.fromString(id) })
                    .limit(1)
                    .map { fromRow(it) }
                    .firstOrNull()
        }
    }

    override fun create(entity: Items): Items {
        transaction(marketPlaceDb) {
            ItemsTable.insert { fromEntity(it, entity) }
        }
        return entity
    }

    override fun update(id: String, entity: Items): Items {
        TODO("Not yet implemented")
    }

    override fun delete(id: String): Boolean {
        TODO("Not yet implemented")
    }
}