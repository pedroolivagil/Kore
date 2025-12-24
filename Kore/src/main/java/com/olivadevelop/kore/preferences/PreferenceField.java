package com.olivadevelop.kore.preferences;

import java.util.Set;

import lombok.Data;

@Data
public final class PreferenceField {
    private final String name;
    private final Class<?> type;
    public static Class<?> getType(int preferenceType) {
        switch (preferenceType) {
            case 0:
                return Boolean.class;
            case 1:
                return Float.class;
            case 2:
                return Integer.class;
            case 3:
                return Long.class;
            case 4:
                return String.class;
            case 5:
                return Set.class;
            case 6:
                return Object.class;
            default:
                return null;
        }
    }
}
