package com.olivadevelop.kore.db.provider;

import android.content.Context;

import com.olivadevelop.kore.db.IDataBaseFactory;
import com.olivadevelop.kore.db.factory.DefaultDataBaseFactory;

public class DataBaseFactoryProvider implements IDataBaseFactoryProvider {
    @Override
    public IDataBaseFactory create(Context context, IDataBaseProvider provider) { return new DefaultDataBaseFactory(context, provider); }
}