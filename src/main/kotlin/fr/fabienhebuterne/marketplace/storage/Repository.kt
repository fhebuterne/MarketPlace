package fr.fabienhebuterne.marketplace.storage

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

interface Repository<T> {
    fun fromRow(row: ResultRow): T
    fun fromEntity(insertTo: UpdateBuilder<Number>, entity: T): UpdateBuilder<Number>
    fun findAll(from: Int?, to: Int?, searchKeyword: String?): List<T>
    fun find(id: String): T?
    fun create(entity: T): T
    fun update(entity: T): T
    fun delete(id: UUID)
    fun countAll(searchKeyword: String?): Int
}