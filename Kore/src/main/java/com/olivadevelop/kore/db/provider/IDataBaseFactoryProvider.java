package com.olivadevelop.kore.db.provider;

import android.content.Context;

import com.olivadevelop.kore.db.IDataBaseFactory;

public interface IDataBaseFactoryProvider {
    IDataBaseFactory create(Context context, IDataBaseProvider provider);
}
