package fr.fabienhebuterne.marketplace.domain.paginated

import org.bukkit.inventory.ItemStack
import java.util.*

abstract class Entity(
    open val id: UUID? = null,
    open val itemStack: ItemStack,
    open val version: Int
)
