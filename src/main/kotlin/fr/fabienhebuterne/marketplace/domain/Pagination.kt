package fr.fabienhebuterne.marketplace.domain

class Pagination<T>(
        val results: List<T>,
        val currentPage: Int = 1,
        val total: Int = 0,
        val resultPerPage: Int = 25
)