package com.cheatdatabase.data;

import androidx.room.TypeConverter;

import java.util.Date;

public class RoomDateConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}