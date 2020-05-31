package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.base.Filter
import fr.fabienhebuterne.marketplace.domain.base.FilterName
import fr.fabienhebuterne.marketplace.domain.base.FilterType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

interface Repository<T> {
    fun fromRow(row: ResultRow): T
    fun fromEntity(insertTo: UpdateBuilder<Number>, entity: T): UpdateBuilder<Number>
    fun findAll(uuid: UUID? = null, from: Int? = null, to: Int? = null, searchKeyword: String? = null, filter: Filter = Filter(FilterName.CREATED_AT, FilterType.DESC)): List<T>
    fun find(id: String): T?
    fun create(entity: T): T
    fun update(entity: T): T
    fun delete(id: UUID)
    fun countAll(uuid: UUID? = null, searchKeyword: String?): Int
}