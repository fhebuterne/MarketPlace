package fr.fabienhebuterne.marketplace.storage

interface Repository<T> {
    fun findAll(): List<T>
    fun find(id: Int): T
    fun create(entity: T): T
    fun update(id: Int, entity: T): T
    fun delete(id: Int): Boolean
}