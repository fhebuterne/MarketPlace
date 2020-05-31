package fr.fabienhebuterne.marketplace.domain.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
        val database: Database,
        val expiration: Expiration
)

@Serializable
data class Expiration(
        val playerToListings: Long,
        val listingsToMails: Long,
        val listingsToMailsNotifCommand: List<String>,
        val mailsToDeleteNotifCommand: List<String>
)
