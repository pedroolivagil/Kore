package com.olivadevelop.kore.db.provider;

import android.content.Context;

import com.olivadevelop.kore.db.IDataBase;

public interface IDataBaseProvider {
    IDataBase provide(Context context);
}
