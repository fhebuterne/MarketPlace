package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import org.bukkit.inventory.ItemStack
import java.util.*

interface ListingsRepository : PaginationRepository<Listings> {
    fun find(sellerUuid: String, itemStack: ItemStack, price: Long): Listings?
    fun findByUUID(sellerUuid: UUID): List<Listings>
}