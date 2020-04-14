package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Items
import fr.fabienhebuterne.marketplace.domain.ItemsTable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ItemsRepositoryImpl(private val marketPlaceDb: Database) : ItemsRepository {
    private val json = Json(JsonConfiguration.Stable)
    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())

    override fun fromRow(row: ResultRow): Items {
        val parse: Map<String, String> = json.parse(mapSerializer, row[ItemsTable.item])

        return Items(
                id = row[ItemsTable.id].value,
                item = LinkedHashMap(parse).toMutableMap()
        )
    }

    override fun fromEntity(insertTo: InsertStatement<Number>, entity: Items): InsertStatement<Number> {
        val mapValues: Map<String, String> = entity.item.mapValues { entry -> entry.value.toString() }
        val itemMutableMap = json.stringify(mapSerializer, mapValues)

        insertTo[ItemsTable.id] = EntityID(entity.id, ItemsTable)
        insertTo[ItemsTable.item] = itemMutableMap
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

    override fun countAll(): Int {
        TODO("Not yet implemented")
    }
}