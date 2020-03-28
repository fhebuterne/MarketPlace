package fr.fabienhebuterne.marketplace.storage;

import java.util.List;

public interface Repository<T> {
    public List<T> findAll();
    public T find(int id);
    public T create(T entity);
    public T update(int id, T entity);
    public boolean delete(int id);
}
