package com.olivadevelop.kore.db.database;

import android.content.Context;

import com.olivadevelop.kore.util.Utils;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DataBaseConfig<DB extends KoreDataBase> {
    private final Class<DB> dbClass;
    private final String dbName;
    private final Context context;
    public DataBaseConfig(Class<DB> dbClass, String dbName, Context context) {
        this.dbClass = Utils.Reflex.getClassTypeArgument(this);
        this.dbName = dbName;
        this.context = context;
    }
}
