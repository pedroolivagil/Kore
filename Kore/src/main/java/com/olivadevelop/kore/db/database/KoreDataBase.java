package com.olivadevelop.kore.db.database;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.olivadevelop.kore.db.converter.Converters;

@TypeConverters({Converters.class})
public abstract class KoreDataBase extends RoomDatabase { }
