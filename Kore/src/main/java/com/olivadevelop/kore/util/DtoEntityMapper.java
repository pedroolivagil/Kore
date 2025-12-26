package com.olivadevelop.kore.util;

import android.util.Log;

import androidx.room.Entity;

import com.olivadevelop.kore.Constants;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface DtoEntityMapper {
    static <T, U> U mapDtoToEntity(T original, U target) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            InstantiationException {
        if (original == null || target == null) { throw new IllegalArgumentException("Original and target must not be null"); }
        Field[] fields = FieldUtils.getAllFields(original.getClass());
        for (Field dtoField : fields) {
            if (Modifier.isStatic(dtoField.getModifiers()) || Modifier.isFinal(dtoField.getModifiers())) { continue; }
            Object value = FieldUtils.readField(dtoField, original, true);
            Field entityField = getTargetField(target, dtoField.getName());
            if (value != null && entityField != null) {
                Class<?> entityFieldType = entityField.getType();
                processField(target, entityFieldType, entityField, value, entityFieldType, DtoEntityMapper::mapDtoToEntity);
            }
        }
        return target;
    }
    static Object mapEntityToDto(Object original, Object target) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            InstantiationException {
        if (original == null || target == null) { throw new IllegalArgumentException("Original and target must not be null"); }
        Field[] fields = FieldUtils.getAllFields(original.getClass());
        for (Field entityField : fields) {
            if (Modifier.isStatic(entityField.getModifiers()) || Modifier.isFinal(entityField.getModifiers())) { continue; }
            Object value = FieldUtils.readField(entityField, original, true);
            Field dtoField = getTargetField(target, entityField.getName());
            if (value != null && dtoField != null) {
                Class<?> entityFieldType = entityField.getType();
                Class<?> dtoFieldType = dtoField.getType();
                processField(target, dtoFieldType, dtoField, value, entityFieldType, DtoEntityMapper::mapEntityToDto);
            }
        }
        return target;
    }
    private static <U> Field getTargetField(U target, String dtoField) {
        Field field = FieldUtils.getField(target.getClass(), dtoField, true);
        if (field == null) {
            for (Field f : FieldUtils.getAllFieldsList(target.getClass())) {
                if (!f.getName().equalsIgnoreCase(dtoField)) { continue; }
                field = FieldUtils.getField(target.getClass(), f.getName(), true);
            }
        }
        return field;
    }
    private static <U> void processField(U target, Class<?> fieldType, Field field, Object value, Class<?> fieldTypeValidator, OnWhenRecursive recursive) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (List.class.isAssignableFrom(fieldTypeValidator)) {
            List<Object> entityList = new ArrayList<>();
            List<?> originalList = (List<?>) value;
            fillListValues(field, originalList, entityList, recursive);
            FieldUtils.writeField(field, target, entityList, true);
        } else if (Set.class.isAssignableFrom(fieldTypeValidator)) {
            Set<Object> entitySet = new HashSet<>();
            Set<?> originalSet = (Set<?>) value;
            fillListValues(field, originalSet, entitySet, recursive);
            FieldUtils.writeField(field, target, entitySet, true);
        } else if (isArray(fieldTypeValidator)) {
            FieldUtils.writeField(field, target, copyArray(fieldType, value), true);
        } else if (isPrimitiveOrWrapper(fieldTypeValidator) || String.class.equals(fieldTypeValidator)) {
            FieldUtils.writeField(field, target, value, true);
        } else {
            Object nestedEntity = fieldType.getDeclaredConstructor().newInstance();
            recursive.run(value, nestedEntity);
            FieldUtils.writeField(field, target, nestedEntity, true);
        }
    }
    static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type != null && (type.isPrimitive() || type.equals(Boolean.class) || type.equals(Byte.class) || type.equals(Character.class) || type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class) || type.equals(Float.class) || type.equals(Double.class));
    }
    private static boolean isArray(Class<?> obj) { return obj != null && obj.isArray(); }
    private static boolean isEntity(Class<?> obj) { return obj != null && !isPrimitiveOrWrapper(obj) && (obj.isAnnotationPresent(Entity.class)); }
    private static Object transformDtoToEntity(Object original, Type entityType, OnWhenRecursive recursive) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, InstantiationException {
        Object target;
        target = ((Class<?>) entityType).getDeclaredConstructor().newInstance();
        recursive.run(original, target);
        return target;
    }
    private static void fillListValues(Field entityField, Collection<?> originalList, Collection<Object> entityList, OnWhenRecursive recursive) {
        Type genericType = entityField.getGenericType();
        Type actualType = genericType instanceof ParameterizedType ? ((ParameterizedType) genericType).getActualTypeArguments()[0] : null;
        if (actualType == null) { return; }
        try {
            for (Object item : originalList) {
                if (isEntity((Class<?>) actualType) || isEntity(item.getClass())) {
                    Object e = transformDtoToEntity(item, actualType, recursive);
                    entityList.add(e);
                } else {
                    entityList.add(item);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.Log.TAG, String.format("%s es lazy y no se pueden obtener los valores", entityField.getName()));
        }
    }
    private static Object copyArray(Class<?> fieldType, Object value) {
        int length = Array.getLength(value);
        Class<?> componentType = fieldType.getComponentType();
        if (componentType == null) { return null; }
        Object entityArray = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) { Array.set(entityArray, i, Array.get(value, i)); }
        return entityArray;
    }
    interface OnWhenRecursive {
        <T, U> void run(T original, U target) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException;
    }
}
