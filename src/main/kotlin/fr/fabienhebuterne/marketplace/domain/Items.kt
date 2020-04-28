package fr.fabienhebuterne.marketplace.domain

import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

data class Items(
        val id: UUID,
        val item: ItemStack
)

object ItemsTable : UUIDTable("marketplace_items") {
    val item = ItemsTable.text("item")
}