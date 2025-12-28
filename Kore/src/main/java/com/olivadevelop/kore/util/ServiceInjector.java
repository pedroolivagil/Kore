package com.olivadevelop.kore.util;

import android.content.Context;
import android.util.Log;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.annotation.DefaultImpl;
import com.olivadevelop.kore.annotation.InjectService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ServiceInjector {
    static void inject(Object target, Context context) {
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            List<Field> declaredFields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(InjectService.class))
                    .collect(Collectors.toList());
            for (Field field : declaredFields) {
                Class<?> type = field.getType();
                DefaultImpl defImpl = type.getAnnotation(DefaultImpl.class);
                if (defImpl == null) { continue; }
                try {
                    Class<?> implType = defImpl.value();
                    Method getMethod = implType.getMethod("get", Context.class);
                    Object service = getMethod.invoke(null, context.getApplicationContext());
                    field.setAccessible(true);
                    field.set(target, service);
                } catch (Exception e) {
                    String message = "Cannot inject service " + type.getName() + " into field " + field.getName() + " of " + clazz.getName();
                    Log.e(Constants.Log.TAG, message, e);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
