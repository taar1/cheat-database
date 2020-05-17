package com.cheatdatabase.data;

import android.net.Uri;

import androidx.room.TypeConverter;

public class RoomUriConverter {

    @TypeConverter
    public static Uri toUri(String uri) {
        return Uri.parse(uri);
    }

    @TypeConverter
    public static String toString(Uri uri) {
        return uri.toString();
    }
}