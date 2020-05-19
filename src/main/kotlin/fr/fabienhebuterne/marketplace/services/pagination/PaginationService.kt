package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.storage.PaginationRepository
import java.util.*

abstract class PaginationService<T : Paginated>(private val paginationRepository: PaginationRepository<T>) {

    val playersView: MutableMap<UUID, Pagination<T>> = mutableMapOf()

    fun nextPage(uuid: UUID): Pagination<T> {
        val pagination = playersView[uuid]
        var currentPage = pagination?.currentPage

        if (currentPage != null && pagination != null) {
            if (currentPage < pagination.maxPage()) {
                currentPage += 1
            }
        } else {
            currentPage = 1
        }

        return getInventoryPaginated(uuid, currentPage)
    }

    fun previousPage(uuid: UUID): Pagination<T> {
        val currentPage = playersView[uuid]?.let {
            if (it.currentPage > 1) {
                it.currentPage.minus(1)
            } else {
                1
            }
        } ?: 1

        return getInventoryPaginated(uuid, currentPage)
    }

    fun getInventoryPaginated(uuid: UUID, currentPage: Int = 1): Pagination<T> {
        var from = 0
        var to = 45

        if (currentPage > 1) {
            from = (currentPage - 1) * 45
            to = from + 45
        }

        val results = paginationRepository.findAll(from, to)
        val countAll = paginationRepository.countAll()
        val pagination = Pagination(
                results,
                currentPage,
                countAll
        )

        playersView[uuid] = pagination

        return pagination
    }
}