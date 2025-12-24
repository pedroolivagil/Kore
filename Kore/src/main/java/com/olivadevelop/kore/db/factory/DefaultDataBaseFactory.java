package com.olivadevelop.kore.db.factory;

import android.content.Context;

import com.olivadevelop.kore.db.provider.IDataBaseProvider;

public class DefaultDataBaseFactory extends DataBaseFactory {
    public DefaultDataBaseFactory(Context context, IDataBaseProvider provider) { super(context, provider); }
}
