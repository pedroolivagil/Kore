package com.olivadevelop.kore.db.dto;

import android.util.Log;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.db.entity.KoreEntity;
import com.olivadevelop.kore.util.DtoEntityMapper;
import com.olivadevelop.kore.util.Utils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public abstract class KoreDTO<T extends KoreEntity> implements Serializable {
    public final T toEntity() {
        try {
            Class<T> t = Utils.Reflex.getClassTypeArgument(this);
            return DtoEntityMapper.mapDtoToEntity(this, t.getDeclaredConstructor().newInstance());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(Constants.Log.TAG, "Error parsing to entity", e);
            throw new RuntimeException(e);
        }
    }
}
