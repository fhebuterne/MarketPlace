package fr.fabienhebuterne.marketplace.storage.mysql

import fr.fabienhebuterne.marketplace.domain.Items
import fr.fabienhebuterne.marketplace.domain.ItemsTable
import fr.fabienhebuterne.marketplace.json.ITEMSTACK_MODULE
import fr.fabienhebuterne.marketplace.json.ItemStackSerializer
import fr.fabienhebuterne.marketplace.storage.ItemsRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ItemsRepositoryImpl(private val marketPlaceDb: Database) : ItemsRepository {
    private val json = Json(JsonConfiguration.Stable, context = ITEMSTACK_MODULE)

    override fun fromRow(row: ResultRow): Items {
        val itemStack: ItemStack = json.parse(ItemStackSerializer, row[ItemsTable.item])

        return Items(
                id = row[ItemsTable.id].value,
                item = itemStack
        )
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: Items): UpdateBuilder<Number> {
        val itemStack = json.stringify(ItemStackSerializer, entity.item)

        insertTo[ItemsTable.id] = EntityID(entity.id, ItemsTable)
        insertTo[ItemsTable.item] = itemStack
        return insertTo
    }

    override fun findAll(from: Int?, to: Int?): List<Items> {
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

    override fun findByItemStack(itemStack: ItemStack): Items? {
        val itemStackString = json.stringify(ItemStackSerializer, itemStack)
        return transaction(marketPlaceDb) {
            ItemsTable.select(Op.build { ItemsTable.item eq itemStackString })
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

    override fun update(entity: Items): Items {
        TODO("Not yet implemented")
    }

    override fun delete(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun countAll(): Int {
        TODO("Not yet implemented")
    }
}