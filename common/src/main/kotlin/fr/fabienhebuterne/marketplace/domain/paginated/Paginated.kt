package fr.fabienhebuterne.marketplace.domain.paginated

import org.bukkit.inventory.ItemStack
import java.util.*

abstract class Paginated(
    override val id: UUID? = null,
    override val itemStack: ItemStack,
    override val version: Int
) : Entity(id, itemStack, version)