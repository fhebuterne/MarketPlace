package fr.fabienhebuterne.marketplace.domain.config

import kotlinx.serialization.Serializable

@Serializable
data class Database(
        val hostname: String = "localhost",
        val database: String = "minecraft",
        val port: Int = 3306,
        val username: String = "minecraft",
        val password: String = ""
)