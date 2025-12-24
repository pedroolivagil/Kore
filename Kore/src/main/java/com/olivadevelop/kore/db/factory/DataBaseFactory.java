package com.olivadevelop.kore.db.factory;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.olivadevelop.kore.db.IDataBase;
import com.olivadevelop.kore.db.IDataBaseFactory;
import com.olivadevelop.kore.db.provider.IDataBaseProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class DataBaseFactory implements IDataBaseFactory {
    private final IDataBase db;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    DataBaseFactory(Context context, IDataBaseProvider provider) {
        this.db = provider.provide(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    @Override
    public <T> void execute(ExecutionMode mode, Function<IDataBase, T> action, ResultSetCallback<T> callback) {
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
    public void execute(ExecutionMode mode, Consumer<IDataBase> db, VoidCallback callback) {
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
    public <T> void executeOnUI(Function<IDataBase, T> db, ResultSetCallback<T> callback) { execute(ExecutionMode.UI, db, callback); }
    @Override
    public void executeOnUI(Consumer<IDataBase> db, VoidCallback callback) { execute(ExecutionMode.UI, db, callback); }
}