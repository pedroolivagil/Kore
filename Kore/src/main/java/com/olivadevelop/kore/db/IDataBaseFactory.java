package com.olivadevelop.kore.db;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IDataBaseFactory {
    <T> void execute(ExecutionMode mode, Function<IDataBase, T> db, ResultSetCallback<T> callback);
    void execute(ExecutionMode mode, Consumer<IDataBase> db, VoidCallback callback);
    <T> void executeOnUI(Function<IDataBase, T> db, ResultSetCallback<T> callback);
    void executeOnUI(Consumer<IDataBase> db, VoidCallback callback);
    interface VoidCallback {
        void onComplete();
    }

    interface ResultSetCallback<T> {
        void onComplete(T result);
    }

    enum ExecutionMode {
        BACKGROUND, UI
    }
}
