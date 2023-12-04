package fr.fabienhebuterne.marketplace.domain.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Config(
    val language: String = "en",
    val database: Database,
    val expiration: Expiration,
    val sellerItemNotifCommand: List<String>,
    val maxDecimalMoney: Int,
    val maxMoneyToSellItem: Double,
    val inventoryLoreMaterial: InventoryLoreMaterial,
    val inventoryValidItem: String,
    val inventoryCancelItem: String
): ConfigType

@Serializable
data class InventoryLoreMaterial(
    val empty: String,
    val filter: String
)

@Serializable
data class Database(
    val hostname: String = "localhost",
    val database: String = "minecraft",
    val port: Int = 3306,
    val username: String = "minecraft",
    val password: String = "",
    val type: DatabaseType = DatabaseType.MYSQL
)

enum class DatabaseType {
    MYSQL, MARIADB, SQLITE
}

@Serializable
data class Expiration(
    val playerToListings: Long = 604800,
    val listingsToMailsEnabled: Boolean = true,
    val listingsToMails: Long = 604800,
    val listingsToMailsNotifCommand: List<String>,
    val mailsToDeleteEnabled: Boolean = true,
    val mailsToDeleteNotifCommand: List<String>
) {
    @Transient
    val allExpirationsDisabled = !listingsToMailsEnabled && !mailsToDeleteEnabled
}
