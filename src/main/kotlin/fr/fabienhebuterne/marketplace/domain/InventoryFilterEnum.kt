package fr.fabienhebuterne.marketplace.domain

import fr.fabienhebuterne.marketplace.domain.base.FilterName
import fr.fabienhebuterne.marketplace.domain.base.FilterType
import fr.fabienhebuterne.marketplace.domain.config.Item
import fr.fabienhebuterne.marketplace.tl

enum class InventoryFilterEnum(
        val order: Int,
        var filterName: FilterName,
        var filterType: FilterType,
        var itemTranslation: Item,
        val inventoryType: InventoryType? = null
) {
    CREATED_AT_DESC(1, FilterName.CREATED_AT, FilterType.DESC, tl.inventoryFilterEnum.createdAt.DESC),
    CREATED_AT_ASC(2, FilterName.CREATED_AT, FilterType.ASC, tl.inventoryFilterEnum.createdAt.ASC),
    EXPIRED_AT_DESC(3, FilterName.EXPIRED_AT, FilterType.DESC, tl.inventoryFilterEnum.expiredAt.DESC),
    EXPIRED_AT_ASC(4, FilterName.EXPIRED_AT, FilterType.ASC, tl.inventoryFilterEnum.expiredAt.ASC),
    PRICE_DESC(5, FilterName.PRICE, FilterType.DESC, tl.inventoryFilterEnum.price.DESC, InventoryType.LISTINGS),
    PRICE_ASC(6, FilterName.PRICE, FilterType.ASC, tl.inventoryFilterEnum.price.ASC, InventoryType.LISTINGS);

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

fun reloadFilterTranslation() {
    InventoryFilterEnum.CREATED_AT_DESC.itemTranslation = tl.inventoryFilterEnum.createdAt.DESC
    InventoryFilterEnum.CREATED_AT_ASC.itemTranslation = tl.inventoryFilterEnum.createdAt.ASC
    InventoryFilterEnum.EXPIRED_AT_DESC.itemTranslation = tl.inventoryFilterEnum.expiredAt.DESC
    InventoryFilterEnum.EXPIRED_AT_ASC.itemTranslation = tl.inventoryFilterEnum.expiredAt.ASC
    InventoryFilterEnum.PRICE_DESC.itemTranslation = tl.inventoryFilterEnum.price.DESC
    InventoryFilterEnum.PRICE_DESC.itemTranslation = tl.inventoryFilterEnum.price.ASC
}

