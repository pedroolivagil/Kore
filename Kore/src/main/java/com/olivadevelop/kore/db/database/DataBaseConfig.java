package com.olivadevelop.kore.db.database;

import android.content.Context;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DataBaseConfig<DB extends KoreDataBase> {
    private final Class<DB> dbClass;
    private final String dbName;
    private final Context context;
}
