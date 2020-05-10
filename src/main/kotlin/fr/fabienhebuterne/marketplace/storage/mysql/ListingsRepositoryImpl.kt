package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.domain.ItemsTable
import fr.fabienhebuterne.marketplace.domain.Listings
import fr.fabienhebuterne.marketplace.domain.ListingsTable
import fr.fabienhebuterne.marketplace.domain.ListingsTable.itemUuid
import fr.fabienhebuterne.marketplace.domain.ListingsTable.price
import fr.fabienhebuterne.marketplace.domain.ListingsTable.quantity
import fr.fabienhebuterne.marketplace.domain.ListingsTable.sellerPseudo
import fr.fabienhebuterne.marketplace.domain.ListingsTable.sellerUuid
import fr.fabienhebuterne.marketplace.domain.ListingsTable.time
import fr.fabienhebuterne.marketplace.domain.ListingsTable.world
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ListingsRepositoryImpl(private val marketPlaceDb: Database) : ListingsRepository {
    override fun fromRow(row: ResultRow) = Listings(
            sellerUuid = row[sellerUuid],
            sellerPseudo = row[sellerPseudo],
            itemUuid = row[itemUuid].value,
            quantity = row[quantity],
            price = row[price],
            world = row[world],
            time = row[time]
    )

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Listings): UpdateBuilder<Number> {
        insertTo[sellerUuid] = entity.sellerUuid
        insertTo[sellerPseudo] = entity.sellerPseudo
        insertTo[itemUuid] = EntityID(entity.itemUuid, ItemsTable)
        insertTo[quantity] = entity.quantity
        insertTo[price] = entity.price
        insertTo[world] = entity.world
        insertTo[time] = entity.time
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

    override fun find(sellerUuid: String, itemUuid: UUID, price: Long): Listings? {
        return transaction(marketPlaceDb) {
            ListingsTable.select {
                (ListingsTable.sellerUuid eq sellerUuid) and
                        (ListingsTable.itemUuid eq itemUuid) and
                        (ListingsTable.price eq price)
            }.map { fromRow(it) }.firstOrNull()
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
                (sellerUuid eq entity.sellerUuid) and
                        (itemUuid eq entity.itemUuid) and
                        (price eq entity.price)
            }) {
                fromEntity(it, entity)
            }
        }
        return entity
    }

    override fun delete(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun countAll(): Int {
        return transaction(marketPlaceDb) {
            ListingsTable.selectAll().count().toInt()
        }
    }
}