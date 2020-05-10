package fr.fabienhebuterne.marketplace.domain

import org.jetbrains.exposed.sql.Table
import java.util.*

data class Listings(
        val sellerUuid: String,
        val sellerPseudo: String,
        val itemUuid: UUID,
        val quantity: Int = 1,
        val price: Long,
        val world: String,
        val time: Long
)

object ListingsTable : Table("marketplace_listings") {
    val sellerUuid = ListingsTable.varchar("seller_uuid", 36)
    val sellerPseudo = ListingsTable.varchar("seller_pseudo", 16)
    val itemUuid = ListingsTable.reference("item_uuid", ItemsTable.id)
    val quantity = ListingsTable.integer("quantity")
    val price = ListingsTable.long("price")
    val world = ListingsTable.varchar("world", 200)
    val time = ListingsTable.long("time")

    override val primaryKey = PrimaryKey(sellerUuid, itemUuid, price, name = "PK_MP_LISTINGS")
}