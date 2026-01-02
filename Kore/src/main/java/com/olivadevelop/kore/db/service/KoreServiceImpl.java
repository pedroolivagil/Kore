package com.olivadevelop.kore.db.service;

import com.olivadevelop.kore.db.database.KoreDataBase;
import com.olivadevelop.kore.db.dto.KoreDTO;
import com.olivadevelop.kore.db.entity.KoreEntity;
import com.olivadevelop.kore.db.factory.KoreDataBaseFactory;

import java.util.Collections;
import java.util.List;

@Deprecated
public abstract class KoreServiceImpl<D extends KoreDTO<V>, V extends KoreEntity, K> implements IKoreService<D, V, K> {
    protected abstract <F extends KoreDataBaseFactory<? extends KoreDataBase>> F getFactory();
    @Override
    public D findOne(K key) { return null; }
    @Override
    public D findOne(V entity) { return null; }
    @Override
    public D findOne(D dto) { return null; }
    @Override
    public List<D> findAll() { return Collections.emptyList(); }
    @Override
    public List<V> findAllEntities() { return Collections.emptyList(); }
    @Override
    public D create(D project) { return null; }
    @Override
    public D update(D project) { return null; }
    @Override
    public void delete(D project) { }
}
