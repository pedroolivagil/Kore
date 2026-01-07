package com.olivadevelop.kore.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;

public final class PreferencesHelper {
    @Getter
    private static PreferencesHelper instance;
    public static void init(IPreferenceProvider provider) {
        if (instance == null) {
            instance = new PreferencesHelper(provider.getContext(), provider.getName());
        }
    }
    private final PreferencesManager manager;
    private PreferencesHelper(Context c, String name) { this.manager = new PreferencesManager(c, name); }
    public void add(PreferenceField field, Object value) {
        SharedPreferences.Editor editor = this.manager.editor();
        if (value instanceof Integer) {
            editor.putInt(field.getName(), (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(field.getName(), (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(field.getName(), (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(field.getName(), (Float) value);
        } else if (value instanceof String) {
            editor.putString(field.getName(), (String) value);
        } else if (value instanceof Set<?>) {
            editor.putStringSet(field.getName(), ((Set<?>) value).stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toSet()));
        }
        editor.apply();
    }
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(PreferenceField field) {
        if (!manager.getPrefs().contains(field.getName())) { return Optional.empty(); }
        try {
            Map<String, ?> all = manager.getPrefs().getAll();
            Object value = all.get(field.getName());
            if (value instanceof Integer) {
                return Optional.of((T) value);
            } else if (value instanceof Boolean) {
                return Optional.of((T) value);
            } else if (value instanceof Long) {
                return Optional.of((T) value);
            } else if (value instanceof Float) {
                return Optional.of((T) value);
            } else if (value instanceof String) {
                return Optional.of((T) String.valueOf(value));
            } else if (value instanceof Set<?>) {
                Set<String> stringSet = ((Set<?>) value).stream().map(Object::toString).collect(Collectors.toSet());
                return Optional.of((T) stringSet);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }
    @SuppressWarnings("unchecked")
    public <T> T get(PreferenceField field, T defaultValue) { return (T) get(field).orElse(defaultValue); }
    @SuppressWarnings("unchecked")
    public <R, T> Optional<T> get(PreferenceField field, Function<R, T> transformer) { return Optional.ofNullable(transformer.apply((R) get(field).orElse(null))); }
    @SuppressWarnings("unchecked")
    public <R, T> T get(PreferenceField field, Function<R, T> transformer, T defaultValue) {
        R r = (R) get(field).orElse(null);
        if (r == null) { return defaultValue; }
        return Optional.of(transformer.apply(r)).orElse(defaultValue);
    }
    public void clearAll() { manager.editor().clear().apply(); }
}