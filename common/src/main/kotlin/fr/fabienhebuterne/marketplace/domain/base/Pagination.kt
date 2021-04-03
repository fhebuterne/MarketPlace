package fr.fabienhebuterne.marketplace.domain.base

import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import java.util.*
import kotlin.math.ceil

data class Pagination<T : Paginated>(
    val results: List<T> = listOf(),
    val currentPage: Int = 1,
    val total: Int = 0,
    val resultPerPage: Int = 45,
    val searchKeyword: String? = null,
    val filter: Filter = Filter(
        FilterName.CREATED_AT,
        FilterType.DESC
    ),
    val showAll: Boolean = false,
    val currentPlayer: UUID,
    val viewPlayer: UUID
) {
    fun maxPage(): Int {
        val maxPage = ceil((total.toDouble() / resultPerPage.toDouble())).toInt()

        return if (maxPage == 0) {
            1
        } else {
            maxPage
        }
    }
}