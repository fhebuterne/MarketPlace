package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Items
import org.bukkit.inventory.ItemStack

interface ItemsRepository : Repository<Items> {
    fun findByItemStack(itemStack: ItemStack): Items?
}