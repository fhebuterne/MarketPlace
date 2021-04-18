package fr.fabienhebuterne.marketplace.domain

import org.bukkit.inventory.InventoryView

data class InventoryOpened(
    val inventoryType: InventoryType,
    val inventoryView: InventoryView
)