package fr.fabienhebuterne.marketplace.domain.config

import kotlinx.serialization.Serializable

@Serializable
data class Translation(
        val errors: Errors,
        val inventoryEnum: InventoryEnum,
        val clickMiddleListingInventoryOne: String,
        val clickMiddleListingInventoryTwo: String,
        val commandAddUsage: String,
        val listingCreated: String,
        val listingUpdated: String,
        val itemBuy: String,
        val cancelBuying: String,
        val cancelSelling: String,
        val searchWaiting: String,
        val listingItemBottomLorePlayer: List<String>,
        val mailItemBottomLorePlayer: List<String>
)

@Serializable
data class Errors(
        val numberNotValid: String,
        val handEmpty: String,
        val notEnoughMoney: String,
        val quantityNotAvailable: String,
        val itemNotExist: String,
        val inventoryFull: String
)

@Serializable
data class InventoryEnum(
        val search: Item,
        val listings: Item,
        val mails: Item,
        val previousPage: Item,
        val nextPage: Item
)

@Serializable
data class Item(
        val displayName: String,
        val lore: MutableList<String> = mutableListOf()
)