package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Listings
import org.bukkit.inventory.ItemStack
import java.util.*

interface ListingsRepository: Repository<Listings> {
    fun find(sellerUuid: String, itemStack: ItemStack, price: Long): Listings?
    fun findByUUID(sellerUuid: UUID): List<Listings>
}