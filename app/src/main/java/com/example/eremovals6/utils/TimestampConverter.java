package com.example.eremovals6.utils;

import androidx.room.TypeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimestampConverter {
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    @TypeConverter
    public static String fromTimestamp(Long value) {
        return value == null ? null : ISO_FORMAT.format(new Date(value));
    }

    @TypeConverter
    public static Long toTimestamp(String value) {
        try {
            return value == null ? null : ISO_FORMAT.parse(value).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
