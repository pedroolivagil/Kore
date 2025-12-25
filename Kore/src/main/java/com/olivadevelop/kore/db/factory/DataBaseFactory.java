package com.olivadevelop.kore.db.factory;

import android.os.Handler;
import android.os.Looper;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.olivadevelop.kore.db.IDataBaseFactory;
import com.olivadevelop.kore.db.database.DataBaseConfig;
import com.olivadevelop.kore.db.database.KoreDataBase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class DataBaseFactory<DB extends KoreDataBase> implements IDataBaseFactory<DB> {
    private final DB db;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    DataBaseFactory(DataBaseConfig<DB> config, Function<RoomDatabase.Builder<DB>, RoomDatabase.Builder<DB>> builder) {
        RoomDatabase.Builder<DB> roomBuilder = Room.databaseBuilder(config.getContext(), config.getDbClass(), config.getDbName());
        if (builder != null) { roomBuilder = builder.apply(roomBuilder); }
        this.db = roomBuilder.build();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    @Override
    public <T> void execute(ExecutionMode mode, Function<DB, T> action, ResultSetCallback<T> callback) {
        executorService.execute(() -> {
            T result = action.apply(db);
            if (mode == ExecutionMode.UI) {
                mainHandler.post(() -> callback.onComplete(result));
            } else {
                callback.onComplete(result);
            }
        });
    }
    @Override
    public void execute(ExecutionMode mode, Consumer<DB> db, VoidCallback callback) {
        executorService.execute(() -> {
            db.accept(this.db);
            if (mode == ExecutionMode.UI) {
                mainHandler.post(callback::onComplete);
            } else {
                callback.onComplete();
            }
        });
    }
    @Override
    public <T> void executeOnUI(Function<DB, T> db, ResultSetCallback<T> callback) { execute(ExecutionMode.UI, db, callback); }
    @Override
    public void executeOnUI(Consumer<DB> db, VoidCallback callback) { execute(ExecutionMode.UI, db, callback); }
}