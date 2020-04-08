package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.Listings

interface ListingsRepository: Repository<Listings> {
    fun find(sellerUuid: String, itemUuid: String, amount: Int, price: Int)
}