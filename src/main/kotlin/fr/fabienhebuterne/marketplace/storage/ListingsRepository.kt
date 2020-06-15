package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import org.bukkit.inventory.ItemStack
import java.util.*

interface ListingsRepository : PaginationRepository<Listings> {
    fun find(sellerUuid: UUID, itemStack: ItemStack, price: Double): Listings?
    fun findByUUID(sellerUuid: UUID): List<Listings>
    fun findUUIDBySellerPseudo(sellerPseudo: String): UUID?
}