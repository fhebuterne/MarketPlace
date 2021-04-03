package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.domain.base.FilterName
import fr.fabienhebuterne.marketplace.domain.base.FilterType
import fr.fabienhebuterne.marketplace.domain.config.InventoryFilterEnumTranslation
import fr.fabienhebuterne.marketplace.domain.config.Item

private val defaultItem = Item("Translation not loaded")

enum class InventoryFilterEnum(
    val order: Int,
    var filterName: FilterName,
    var filterType: FilterType,
    var itemTranslation: Item = defaultItem,
    val inventoryType: InventoryType? = null
) {
    CREATED_AT_DESC(1, FilterName.CREATED_AT, FilterType.DESC),
    CREATED_AT_ASC(2, FilterName.CREATED_AT, FilterType.ASC),
    EXPIRED_AT_DESC(3, FilterName.EXPIRED_AT, FilterType.DESC),
    EXPIRED_AT_ASC(4, FilterName.EXPIRED_AT, FilterType.ASC),
    PRICE_DESC(5, FilterName.PRICE, FilterType.DESC, inventoryType = InventoryType.LISTINGS),
    PRICE_ASC(6, FilterName.PRICE, FilterType.ASC, inventoryType = InventoryType.LISTINGS);

    companion object {
        fun findByNameAndType(filterName: FilterName, filterType: FilterType): InventoryFilterEnum {
            return values().find { it.filterName == filterName && it.filterType == filterType } ?: CREATED_AT_DESC
        }

        fun next(currentOrder: Int, inventoryType: InventoryType? = null): InventoryFilterEnum {
            val nextOrder = currentOrder + 1
            var inventoryFilterEnum = if (nextOrder > values().size) {
                getByOrder(1)
            } else {
                getByOrder(nextOrder)
            }

            if (inventoryType != null && inventoryFilterEnum.inventoryType != null && inventoryFilterEnum.inventoryType != inventoryType) {
                inventoryFilterEnum = next(nextOrder, inventoryType)
            }

            return inventoryFilterEnum
        }

        private fun getByOrder(order: Int): InventoryFilterEnum {
            return values().find { it.order == order } ?: CREATED_AT_DESC
        }
    }
}

fun loadInventoryFilterTranslation(inventoryFilterEnum: InventoryFilterEnumTranslation) {
    InventoryFilterEnum.CREATED_AT_DESC.itemTranslation = inventoryFilterEnum.createdAt.DESC
    InventoryFilterEnum.CREATED_AT_ASC.itemTranslation = inventoryFilterEnum.createdAt.ASC
    InventoryFilterEnum.EXPIRED_AT_DESC.itemTranslation = inventoryFilterEnum.expiredAt.DESC
    InventoryFilterEnum.EXPIRED_AT_ASC.itemTranslation = inventoryFilterEnum.expiredAt.ASC
    InventoryFilterEnum.PRICE_DESC.itemTranslation = inventoryFilterEnum.price.DESC
    InventoryFilterEnum.PRICE_ASC.itemTranslation = inventoryFilterEnum.price.ASC
}

