package com.example.doaaa.tripplannerv000;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.doaaa.tripplannerv000.TripContract.TripEntry;

public class TripProvider extends ContentProvider {
    /** Tag for the log messages */
    public static final String LOG_TAG = TripProvider.class.getSimpleName();
    private TripDBHelper mDbHelper;

    public static final int TRIPS = 100;

    public static final int TRIPS_ID = 101;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_TRIPS, TRIPS);

        sUriMatcher.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_TRIPS + "/#", TRIPS_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the trips database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new TripDBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case TRIPS:
                // For the TRIPS code, query the trips table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the trips table.
                // TODO: Perform database query on trips table
                cursor = database.query(TripEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            case TRIPS_ID:
                // For the trips_id code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.doaaa.tripplannerv000/tripplannerv000/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = TripEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the trips table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(TripEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRIPS:
                return insertTrip(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a trip into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertTrip(Uri uri, ContentValues values) {
        String name = values.getAsString(TripEntry.COLUMN_TRIP_NAME);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tet requires a name");
        }

        String sPoint = values.getAsString(TripEntry.COLUMN_START_POINT);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Trip requires start point");
        }

        String ePoint = values.getAsString(TripEntry.COLUMN_END_POINT);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Trip requires end point");
        }

        String tripTime = values.getAsString(TripEntry.COLUMN_TIME);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Trip requires time");
        }

        Integer type = values.getAsInteger(TripEntry.COLUMN_TRIP_TYPE);
        if (type == null || !TripEntry.isValidTripType(type)) {
            throw new IllegalArgumentException("trip requires a type");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(TripEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
//notify all the listeners that the data has changed for the trip content uri
        //
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRIPS:
                return updateTrip(uri, contentValues, selection, selectionArgs);
            case TRIPS_ID:
                // For the TRIP_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TripEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateTrip(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateTrip(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(TripEntry.COLUMN_TRIP_NAME)) {
            String name = values.getAsString(TripEntry.COLUMN_TRIP_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Trip requires a name");
            }
        }

        if (values.containsKey(TripEntry.COLUMN_START_POINT)) {
            String name = values.getAsString(TripEntry.COLUMN_START_POINT);
            if (name == null) {
                throw new IllegalArgumentException("Trip requires start point");
            }
        }

        if (values.containsKey(TripEntry.COLUMN_END_POINT)) {
            String name = values.getAsString(TripEntry.COLUMN_END_POINT);
            if (name == null) {
                throw new IllegalArgumentException("Trip requires end point");
            }
        }

        if (values.containsKey(TripEntry.COLUMN_TIME)) {
            String name = values.getAsString(TripEntry.COLUMN_TIME);
            if (name == null) {
                throw new IllegalArgumentException("Trip requires time");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(TripEntry.COLUMN_TRIP_TYPE)) {
            Integer type = values.getAsInteger(TripEntry.COLUMN_TRIP_TYPE);
            if (type == null || !TripEntry.isValidTripType(type)) {
                throw new IllegalArgumentException("Trip requires valid type");
            }
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(TripEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRIPS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TripEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRIPS_ID:
                // Delete a single row given by the ID in the URI
                selection = TripEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(TripEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRIPS:
                return TripEntry.CONTENT_LIST_TYPE;
            case TRIPS_ID:
                return TripEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

