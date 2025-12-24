package com.olivadevelop.kore.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import lombok.Getter;

@Getter
public final class PreferencesManager {
    private final SharedPreferences prefs;
    public PreferencesManager(Context c, String prefsName) { prefs = c.getSharedPreferences(prefsName, Context.MODE_PRIVATE); }
    public SharedPreferences.Editor editor() {
        return prefs.edit();
    }
}