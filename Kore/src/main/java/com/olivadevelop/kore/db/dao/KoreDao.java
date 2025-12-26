package com.olivadevelop.kore.db.dao;

import com.olivadevelop.kore.db.entity.KoreEntity;

import java.util.List;

public interface KoreDao<T extends KoreEntity, K> {
    K persist(T entity);
    List<K> persist(List<T> entities);
    void merge(T entity);
    void merge(List<T> entity);
    void remove(T entity);
    void remove(List<T> entity);
    T findOne(int id);
    List<T> findAll();
}
