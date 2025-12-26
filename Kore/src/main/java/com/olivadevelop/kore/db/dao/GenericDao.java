package com.olivadevelop.kore.db.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Transaction;
import androidx.room.Update;

import com.olivadevelop.kore.db.entity.KoreEntity;

import java.util.List;

public interface GenericDao<T extends KoreEntity, K> {
    @Insert
    @Transaction
    K persist(T entity);
    @Insert
    @Transaction
    List<K> persist(List<T> entities);
    @Update
    @Transaction
    void merge(T entity);
    @Update
    @Transaction
    void merge(List<T> entity);
    @Delete
    @Transaction
    void remove(T entity);
    @Delete
    @Transaction
    void remove(List<T> entity);
    @Transaction
    T findOne(int id);
    @Transaction
    List<T> findAll();
}
