package fr.fabienhebuterne.marketplace.storage

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

interface Repository<T> {
    fun fromRow(row: ResultRow): T
    fun fromEntity(insertTo: InsertStatement<Number>, entity: T): InsertStatement<Number>
    fun findAll(): List<T>
    fun find(id: String): T?
    fun create(entity: T): T
    fun update(id: String, entity: T): T
    fun delete(id: String): Boolean
}