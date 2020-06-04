package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.base.EntityService
import fr.fabienhebuterne.marketplace.storage.PaginationRepository
import java.util.*

abstract class PaginationService<T : Paginated>(private val paginationRepository: PaginationRepository<T>) : EntityService<T>(paginationRepository) {

    val playersView: MutableMap<UUID, Pagination<T>> = mutableMapOf()

    fun nextPage(uuid: UUID): Pagination<T> {
        val pagination = playersView[uuid]
        val currentPage = pagination?.currentPage

        val returnPaginated = pagination?.let {
            if (it.currentPage < it.maxPage()) {
                it.copy(currentPage = currentPage?.plus(1) ?: 1)
            } else {
                it.copy(currentPage = 1)
            }
        } ?: Pagination(currentPage = 1, currentPlayer = uuid, viewPlayer = uuid)

        return getPaginated(pagination = returnPaginated)
    }

    fun previousPage(uuid: UUID): Pagination<T> {
        val paginated = playersView[uuid]?.let {
            if (it.currentPage > 1) {
                it.copy(currentPage = it.currentPage.minus(1))
            } else {
                it.copy(currentPage = 1)
            }
        } ?: Pagination(currentPage = 1, currentPlayer = uuid, viewPlayer = uuid)

        return getPaginated(pagination = paginated)
    }

    fun getPaginated(
            from: Int = 0,
            to: Int = 45,
            pagination: Pagination<T>
    ): Pagination<T> {
        var fromInt = from
        var toInt = to
        var currentPageInt = pagination.currentPage

        val countAll = if (!pagination.showAll) {
            paginationRepository.countAll(pagination.currentPlayer, pagination.searchKeyword)
        } else {
            paginationRepository.countAll(searchKeyword = pagination.searchKeyword)
        }

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

        val results = if (!pagination.showAll) {
            paginationRepository.findAll(pagination.currentPlayer, fromInt, toInt, pagination.searchKeyword, pagination.filter)
        } else {
            paginationRepository.findAll(from = fromInt, to = toInt, searchKeyword = pagination.searchKeyword, filter = pagination.filter)
        }

        val paginationUpdated = pagination.copy(
                results,
                currentPageInt,
                countAll
        )

        playersView[pagination.viewPlayer] = paginationUpdated

        return paginationUpdated
    }
}