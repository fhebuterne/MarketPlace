package fr.fabienhebuterne.marketplace.services.base

import fr.fabienhebuterne.marketplace.domain.base.Filter
import fr.fabienhebuterne.marketplace.storage.Repository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

abstract class EntityService<T>(private val repository: Repository<T>) : Repository<T> {

    override fun fromRow(row: ResultRow): T {
        return repository.fromRow(row)
    }

    override fun fromEntity(insertTo: UpdateBuilder<Number>, entity: T): UpdateBuilder<Number> {
        return repository.fromEntity(insertTo, entity)
    }

    override fun findAll(uuid: UUID?, from: Int?, to: Int?, searchKeyword: String?, filter: Filter): List<T> {
        return repository.findAll(uuid, from, to, searchKeyword, filter)
    }

    override fun find(id: String): T? {
        return repository.find(id)
    }

    override fun create(entity: T): T {
        return repository.create(entity)
    }

    override fun update(entity: T): T {
        return repository.update(entity)
    }

    override fun delete(id: UUID) {
        return repository.delete(id)
    }

    override fun countAll(uuid: UUID?, searchKeyword: String?): Int {
        return repository.countAll(uuid, searchKeyword)
    }

}