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

        return getPaginated(uuid, currentPage)
    }

    fun previousPage(uuid: UUID): Pagination<T> {
        val currentPage = playersView[uuid]?.let {
            if (it.currentPage > 1) {
                it.currentPage.minus(1)
            } else {
                1
            }
        } ?: 1

        return getPaginated(uuid, currentPage)
    }

    fun getPaginated(uuid: UUID, currentPage: Int = 1, from: Int = 0, to: Int = 45, resultPerPage: Int = 45): Pagination<T> {
        var fromInt = from
        var toInt = to
        var currentPageInt = currentPage

        val countAll = paginationRepository.countAll()

        if (currentPageInt > 1) {
            fromInt = (currentPageInt - 1) * to
            toInt = from + to
        }

        // If currentPage doens't have result
        if (countAll < fromInt) {
            currentPageInt = 1
            fromInt = from
            toInt = to
        }

        val results = paginationRepository.findAll(fromInt, toInt)
        val pagination = Pagination(
                results,
                currentPageInt,
                countAll,
                resultPerPage
        )

        playersView[uuid] = pagination

        return pagination
    }
}