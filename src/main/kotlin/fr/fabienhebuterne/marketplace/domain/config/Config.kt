package fr.fabienhebuterne.marketplace.domain.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(val database: Database)