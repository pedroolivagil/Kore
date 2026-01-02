package com.olivadevelop.kore.db.service;

import com.olivadevelop.kore.db.dto.KoreDTO;
import com.olivadevelop.kore.db.entity.KoreEntity;

import java.util.List;

@Deprecated
public interface IKoreService<D extends KoreDTO<V>, V extends KoreEntity, K> {
    D findOne(K key);
    D findOne(V entity);
    D findOne(D dto);
    List<D> findAll();
    List<V> findAllEntities();
    D create(D project);
    D update(D project);
    void delete(D project);
}
