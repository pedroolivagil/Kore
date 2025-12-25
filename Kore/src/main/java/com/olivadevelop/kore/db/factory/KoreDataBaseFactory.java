package com.olivadevelop.kore.db.factory;

import androidx.room.RoomDatabase;

import com.olivadevelop.kore.db.database.DataBaseConfig;
import com.olivadevelop.kore.db.database.KoreDataBase;

import java.util.function.Function;

public abstract class KoreDataBaseFactory<DB extends KoreDataBase> extends DataBaseFactory<DB> {
    public KoreDataBaseFactory(DataBaseConfig<DB> config, Function<RoomDatabase.Builder<DB>, RoomDatabase.Builder<DB>> builder) { super(config, builder); }
}
