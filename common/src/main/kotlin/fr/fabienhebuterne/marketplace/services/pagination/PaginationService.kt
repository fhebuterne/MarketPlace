package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.base.EntityService
import fr.fabienhebuterne.marketplace.storage.PaginationRepository
import java.util.*

abstract class PaginationService<T : Paginated>(
    private val paginationRepository: PaginationRepository<T>,
    private val marketPlace: MarketPlace
) : EntityService<T>(paginationRepository, marketPlace) {

    val playersView: MutableMap<UUID, Pagination<T>> = mutableMapOf()

    fun nextPage(uuid: UUID): Pagination<T> {
        val pagination = playersView[uuid]
        val currentPage = pagination?.currentPage

        val returnPaginated = pagination?.let {
            if (it.currentPage < it.maxPage()) {
                it.copy(currentPage = currentPage?.plus(1) ?: 1)
            } else {
                it.copy(currentPage = it.maxPage())
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
        pagination: Pagination<T>
    ): Pagination<T> {
        var fromInt = 0
        var toInt = pagination.resultPerPage
        var currentPageInt = pagination.currentPage

        val countAll = if (!pagination.showAll) {
            paginationRepository.countAll(pagination.currentPlayer, pagination.searchKeyword)
        } else {
            paginationRepository.countAll(searchKeyword = pagination.searchKeyword)
        }

        if (currentPageInt > 1) {
            fromInt = (currentPageInt - 1) * pagination.resultPerPage
            toInt = pagination.resultPerPage
        }

        // If currentPage doens't have result
        if (countAll < fromInt) {
            currentPageInt = 1
            fromInt = 0
            toInt = pagination.resultPerPage
        }

        val results = if (!pagination.showAll) {
            paginationRepository.findAll(
                pagination.currentPlayer,
                fromInt,
                toInt,
                pagination.searchKeyword,
                pagination.filter
            )
        } else {
            paginationRepository.findAll(
                from = fromInt,
                to = toInt,
                searchKeyword = pagination.searchKeyword,
                filter = pagination.filter
            )
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