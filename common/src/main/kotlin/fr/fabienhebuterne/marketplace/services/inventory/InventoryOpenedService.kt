package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.domain.InventoryOpened
import java.util.*

data class InventoryOpenedService(
    val inventoryOpened: MutableMap<UUID, InventoryOpened> = mutableMapOf()
)