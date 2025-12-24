package com.olivadevelop.kore.db.converter;

import androidx.room.TypeConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Converters {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    @TypeConverter
    public static Date fromTimestamp(Long value) { return value == null ? null : new Date(value); }
    @TypeConverter
    public static Long dateToTimestamp(Date date) { return date == null ? null : date.getTime(); }
    @TypeConverter
    public static LocalDateTime fromString(String value) { return value == null ? null : LocalDateTime.parse(value, formatter); }
    @TypeConverter
    public static String toString(LocalDateTime value) { return value == null ? null : value.format(formatter); }
}