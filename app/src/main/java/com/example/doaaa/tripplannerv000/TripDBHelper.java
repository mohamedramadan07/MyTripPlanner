package com.example.doaaa.tripplannerv000;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.doaaa.tripplannerv000.TripContract.TripEntry;

public class TripDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = TripDBHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "trips.db";
    private static final int DATABASE_VERSION = 1;

    public TripDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_TRIPS_TABLE =  "CREATE TABLE " + TripEntry.TABLE_NAME + " ("
                + TripEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TripEntry.COLUMN_TRIP_NAME  + " TEXT NOT NULL, "
                + TripEntry.COLUMN_START_POINT + " TEXT NOT NULL, "
                + TripEntry.COLUMN_END_POINT + " TEXT NOT NULL, "
                + TripEntry.COLUMN_TIME + " TEXT NOT NULL, "
                + TripEntry.COLUMN_NOTES + " TEXT , "
                +TripEntry.COLUMN_TRIP_TYPE + " INTEGER NOT NULL );";
        db.execSQL(SQL_CREATE_TRIPS_TABLE);}

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}

