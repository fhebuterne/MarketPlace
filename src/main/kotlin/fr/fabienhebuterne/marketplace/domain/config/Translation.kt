package fr.fabienhebuterne.marketplace.domain.config

import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import kotlinx.serialization.Serializable

@Serializable
data class Translation(
        val errors: Errors,
        val inventoryEnum: InventoryEnum,
        val clickMiddleListingInventoryOne: String,
        val clickMiddleListingInventoryTwo: String,
        val commandAddUsage: String,
        val commandReloadStart: String,
        val commandReloadFinish: String,
        val listingCreated: String,
        val listingUpdated: String,
        val itemBuy: String,
        val cancelBuying: String,
        val cancelSelling: String,
        val searchWaiting: String,
        val listingItemBottomLoreSeller: List<String>,
        val listingItemBottomLorePlayer: List<String>,
        val mailItemBottomLorePlayer: List<String>,
        val commandHelp: List<String>,
        val logs: LogsTranslation
)

@Serializable
data class Errors(
        val numberNotValid: String,
        val handEmpty: String,
        val notEnoughMoney: String,
        val quantityNotAvailable: String,
        val itemNotExist: String,
        val inventoryFull: String,
        val reloadNotAvailable: String
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

@Serializable
data class LogsTranslation(
        val header: String,
        val footer: String,
        val previousPageExist: String,
        val previousPageNotExist: String,
        val nextPageExist: String,
        val nextPageNotExist: String,
        val previousPage: String,
        val nextPage: String,
        val prefix: String,
        val prefixHover: String,
        val type: MutableMap<LogType, String> = mutableMapOf(),
        val message: MutableMap<LogType, String> = mutableMapOf()
)