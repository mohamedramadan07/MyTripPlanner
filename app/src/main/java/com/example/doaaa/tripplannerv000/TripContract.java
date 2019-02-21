package com.example.doaaa.tripplannerv000;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class TripContract {

    public static final String CONTENT_AUTHORITY = "com.example.doaaa.tripplannerv000";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TRIPS = "tripplannerv000";
    private TripContract() {}

    public static final class TripEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRIPS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRIPS;


        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRIPS;

        public final static String TABLE_NAME = "trips";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_TRIP_NAME ="name";
        public final static String COLUMN_START_POINT ="Spoint";
        public final static String COLUMN_END_POINT ="Epoint";
        public final static String COLUMN_TIME ="time";
        public final static String COLUMN_NOTES ="notes";
        public final static String COLUMN_TRIP_TYPE ="type";
        public static final int TYPE_UNKNOWN = 0;
        public static final int ONE_DIRECTION = 1;
        public static final int ROUND_TRIP = 2;

        public static boolean isValidTripType(int type) {
            if (type==ONE_DIRECTION || type == ROUND_TRIP ) {
                return true;
            }
            return false;}
    }

}

