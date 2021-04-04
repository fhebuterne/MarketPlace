package fr.fabienhebuterne.marketplace.domain.config

enum class ConfigPlaceholder(val placeholder: String) {
    ADMIN_PSEUDO("{{adminPseudo}}"),
    PLAYER_PSEUDO("{{playerPseudo}}"),
    PLAYER_UUID("{{playerUUID}}"),
    SELLER_PSEUDO("{{sellerPseudo}}"),
    QUANTITY("{{quantity}}"),
    ADDED_QUANTITY("{{addedQuantity}}"),
    TOTAL_QUANTITY("{{totalQuantity}}"),
    ITEM_STACK("{{itemStack}}"),
    PRICE("{{price}}"),
    UNIT_PRICE("{{unitPrice}}"),
    PREVIOUS_PAGE_BOOLEAN("%previousPage%"),
    NEXT_PAGE_BOOLEAN("%nextPage%"),
    CURRENT_PAGE("{{currentPage}}"),
    MAX_PAGE("{{maxPage}}"),
    TOTAL("{{total}}"),
    EXPIRATION("{{expiration}}"),
    EXPIRATION_BOOLEAN("%expiration%"),
    MIDDLE_BOOLEAN("%middle%"),
    RIGHT_BOOLEAN("%right%"),
    LOG_TYPE("{{logType}}"),
    FROM_LOCATION("{{fromLocation}}"),
    TO_LOCATION("{{toLocation}}"),
    CREATED_AT("{{createdAt}}")
}