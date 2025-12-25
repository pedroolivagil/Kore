package com.olivadevelop.kore.db;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IDataBaseFactory<DB> {
    <T> void execute(ExecutionMode mode, Function<DB, T> db, ResultSetCallback<T> callback);
    void execute(ExecutionMode mode, Consumer<DB> db, VoidCallback callback);
    <T> void executeOnUI(Function<DB, T> db, ResultSetCallback<T> callback);
    void executeOnUI(Consumer<DB> db, VoidCallback callback);
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
