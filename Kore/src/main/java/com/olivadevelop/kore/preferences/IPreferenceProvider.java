package com.olivadevelop.kore.preferences;

import android.content.Context;

public interface IPreferenceProvider {
    Context getContext();
    String getName();
}
