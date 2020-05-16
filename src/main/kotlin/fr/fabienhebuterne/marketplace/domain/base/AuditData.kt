package fr.fabienhebuterne.marketplace.domain.base

data class AuditData(
        val createdAt: Long,
        val updatedAt: Long,
        val expiredAt: Long
)