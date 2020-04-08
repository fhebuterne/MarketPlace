package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.ItemsTable
import fr.fabienhebuterne.marketplace.domain.Listings
import fr.fabienhebuterne.marketplace.domain.ListingsTable
import fr.fabienhebuterne.marketplace.domain.ListingsTable.amount
import fr.fabienhebuterne.marketplace.domain.ListingsTable.itemUuid
import fr.fabienhebuterne.marketplace.domain.ListingsTable.price
import fr.fabienhebuterne.marketplace.domain.ListingsTable.quantity
import fr.fabienhebuterne.marketplace.domain.ListingsTable.sellerPseudo
import fr.fabienhebuterne.marketplace.domain.ListingsTable.sellerUuid
import fr.fabienhebuterne.marketplace.domain.ListingsTable.time
import fr.fabienhebuterne.marketplace.domain.ListingsTable.world
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction

class ListingsRepositoryImpl(private val marketPlaceDb: Database) : ListingsRepository {
    override fun fromRow(row: ResultRow) = Listings(
            sellerUuid = row[sellerUuid],
            sellerPseudo = row[sellerPseudo],
            itemUuid = row[itemUuid].value,
            amount = row[amount],
            quantity = row[quantity],
            price = row[price],
            world = row[world],
            time = row[time]
    )

    override fun fromEntity(insertTo: InsertStatement<Number>, entity: Listings): InsertStatement<Number> {
        insertTo[sellerUuid] = entity.sellerUuid
        insertTo[sellerPseudo] = entity.sellerPseudo
        insertTo[itemUuid] = EntityID(entity.itemUuid, ItemsTable)
        insertTo[amount] = entity.amount
        insertTo[quantity] = entity.quantity
        insertTo[price] = entity.price
        insertTo[world] = entity.world
        insertTo[time] = entity.time
        return insertTo
    }

    override fun findAll(): List<Listings> {
        return transaction(marketPlaceDb) {
            ListingsTable.selectAll().map { fromRow(it) }
        }
    }

    override fun find(id: String): Listings? {
        TODO("Not yet implemented")
    }

    override fun find(sellerUuid: String, itemUuid: String, amount: Int, price: Int) {
        TODO("Not yet implemented")
    }

    override fun create(entity: Listings): Listings {
        transaction(marketPlaceDb) {
            ListingsTable.insert { fromEntity(it, entity) }
        }
        return entity
    }

    override fun update(id: String, entity: Listings): Listings {
        TODO("Not yet implemented")
    }

    override fun delete(id: String): Boolean {
        TODO("Not yet implemented")
    }
}