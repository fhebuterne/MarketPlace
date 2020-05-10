package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Listings
import java.util.*

interface ListingsRepository: Repository<Listings> {
    fun find(sellerUuid: String, itemUuid: UUID, price: Long): Listings?
    fun findByUUID(sellerUuid: UUID): List<Listings>
}