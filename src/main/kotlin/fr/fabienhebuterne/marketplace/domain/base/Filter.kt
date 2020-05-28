package fr.fabienhebuterne.marketplace.domain.base

data class Filter(
        val filterName: FilterName,
        val filterType: FilterType
)

enum class FilterType {
    ASC,
    DESC
}

enum class FilterName {
    CREATED_AT,
    EXPIRED_AT,
    PRICE
}