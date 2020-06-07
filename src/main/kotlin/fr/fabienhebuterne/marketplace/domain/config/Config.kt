package fr.fabienhebuterne.marketplace.domain.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
        val database: Database,
        val expiration: Expiration
)

@Serializable
data class Database(
        val hostname: String = "localhost",
        val database: String = "minecraft",
        val port: Int = 3306,
        val username: String = "minecraft",
        val password: String = ""
)

@Serializable
data class Expiration(
        val playerToListings: Long = 604800,
        val listingsToMails: Long = 604800,
        val listingsToMailsNotifCommand: List<String>,
        val mailsToDeleteNotifCommand: List<String>
)
