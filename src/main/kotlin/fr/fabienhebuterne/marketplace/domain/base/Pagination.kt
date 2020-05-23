package fr.fabienhebuterne.marketplace.domain.base

import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import kotlin.math.ceil

data class Pagination<T : Paginated>(
        val results: List<T>,
        val currentPage: Int = 1,
        val total: Int = 0,
        val resultPerPage: Int = 45
) {
    fun maxPage(): Int {
        return ceil((total.toDouble() / resultPerPage.toDouble())).toInt()
    }
}