package fr.fabienhebuterne.marketplace.services

import fr.fabienhebuterne.marketplace.domain.Listings
import fr.fabienhebuterne.marketplace.domain.Pagination
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import java.util.*

class ListingsService(private val listingsRepository: ListingsRepository) {

    private val playersView: MutableMap<UUID, Pagination<Listings>> = mutableMapOf()

    fun nextPage(uuid: UUID): Pagination<Listings> {
        val currentPage = playersView[uuid]?.currentPage?.plus(1) ?: 1
        return getListingsPaginated(uuid, currentPage)
    }

    fun previousPage(uuid: UUID): Pagination<Listings> {
        val currentPage = playersView[uuid]?.let {
            if (it.currentPage > 1) {
                it.currentPage.minus(1)
            } else {
                1
            }
        } ?: 1

        return getListingsPaginated(uuid, currentPage)
    }

    fun getListingsPaginated(uuid: UUID, currentPage: Int = 1): Pagination<Listings> {
        var from = 0
        var to = 45

        if (currentPage > 1) {
            from = (currentPage - 1) * 45
            to = from + 45
        }

        val results = listingsRepository.findAll(from, to)
        val countAll = listingsRepository.countAll()
        val pagination = Pagination(
                results,
                currentPage,
                countAll
        )

        playersView[uuid] = pagination

        return pagination
    }
}