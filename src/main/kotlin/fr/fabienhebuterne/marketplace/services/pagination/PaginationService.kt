package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.storage.PaginationRepository
import java.util.*

abstract class PaginationService<T : Paginated>(private val paginationRepository: PaginationRepository<T>) {

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
        } ?: Pagination(currentPage = 1)

        return getPaginated(uuid, pagination = returnPaginated)
    }

    fun previousPage(uuid: UUID): Pagination<T> {
        val paginated = playersView[uuid]?.let {
            if (it.currentPage > 1) {
                it.copy(currentPage = it.currentPage.minus(1))
            } else {
                it.copy(currentPage = 1)
            }
        } ?: Pagination(currentPage = 1)

        return getPaginated(uuid, pagination = paginated)
    }

    fun getPaginated(
            uuid: UUID,
            from: Int = 0,
            to: Int = 45,
            pagination: Pagination<T> = Pagination()
    ): Pagination<T> {
        var fromInt = from
        var toInt = to
        var currentPageInt = pagination.currentPage

        val countAll = paginationRepository.countAll(pagination.searchKeyword)

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

        val results = paginationRepository.findAll(fromInt, toInt, pagination.searchKeyword, pagination.filter)
        val paginationUpdated = pagination.copy(
                results,
                currentPageInt,
                countAll
        )

        playersView[uuid] = paginationUpdated

        return paginationUpdated
    }
}