package com.example.doaaa.tripplannerv000;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doaaa.tripplannerv000.TripContract.TripEntry;



public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TRIP_LOADER = 0;

    TripCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the trip data
        ListView tripListView = (ListView) findViewById(R.id.list);


        // Setup an Adapter to create a list item for each row of trip data in the Cursor.
        // There is no trip data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new TripCursorAdapter(this, null);
        tripListView.setAdapter(mCursorAdapter);



        // Kick off the loader
        getLoaderManager().initLoader(TRIP_LOADER, null, this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAllTrips();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllTrips() {
        int rowsDeleted = getContentResolver().delete(TripEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from trips database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                TripEntry._ID,
                TripEntry.COLUMN_TRIP_NAME,
                TripEntry.COLUMN_START_POINT,
                TripEntry.COLUMN_END_POINT,
                TripEntry.COLUMN_TIME,
                TripEntry.COLUMN_TRIP_TYPE,
                TripEntry.COLUMN_NOTES };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                TripEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link TripCursorAdapter} with this new cursor containing updated trip data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    public class TripCursorAdapter extends CursorAdapter {

        ImageButton editBtn;
        ImageButton deleteBtn;
        ImageButton viewBtn;
        Uri currentTripUri;
        Context con;
        Cursor cur;

        public TripCursorAdapter(Context context, Cursor c) {
            super(context, c, 0 /* flags */);
            con=context;
            cur=c;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }


        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
            // Find fields to populate in inflated template
            TextView nameTextView = (TextView) view.findViewById(R.id.viewTripName);
            TextView sPointTextView = (TextView) view.findViewById(R.id.tripStartPoint);
            TextView ePointTextView=(TextView) view.findViewById(R.id.tripEndPoint);
            TextView timeTextView =(TextView) view.findViewById(R.id.tripTime);
            editBtn=(ImageButton) view.findViewById(R.id.editButton);
            deleteBtn=(ImageButton) view.findViewById(R.id.deleteButton);
            viewBtn=(ImageButton) view.findViewById(R.id.viewButton);
            // Extract properties from cursor
            final String tripName = cursor.getString(cursor.getColumnIndexOrThrow(TripEntry.COLUMN_TRIP_NAME));
            final String sPoint = cursor.getString(cursor.getColumnIndexOrThrow(TripEntry.COLUMN_START_POINT));
            final String ePoint= cursor.getString(cursor.getColumnIndexOrThrow(TripEntry.COLUMN_END_POINT));
            final String tripTime=cursor.getString(cursor.getColumnIndexOrThrow(TripEntry.COLUMN_TIME));
            final String tripType=cursor.getString(cursor.getColumnIndexOrThrow(TripEntry.COLUMN_TRIP_TYPE));
            final String tripNotes=cursor.getString(cursor.getColumnIndexOrThrow(TripEntry.COLUMN_NOTES));
            final int pos=cursor.getPosition();


            // Populate fields with extracted properties
            nameTextView.setText(tripName);
            sPointTextView.setText(sPoint);
            ePointTextView.setText(ePoint);
            timeTextView.setText(tripTime);

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent= new Intent(view.getContext(), EditorActivity.class);
                    currentTripUri = ContentUris.withAppendedId(TripEntry.CONTENT_URI, getItemId(pos));
                    intent.setData(currentTripUri);
                    con.startActivity(intent);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentTripUri = ContentUris.withAppendedId(TripEntry.CONTENT_URI, getItemId(pos));
                    showDeleteConfirmationDialog(currentTripUri);
                }
            });

            viewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentViewTrip= new Intent(con, TripDetailsActivity.class);
                    currentTripUri = ContentUris.withAppendedId(TripEntry.CONTENT_URI, getItemId(pos));
                    intentViewTrip.setData(currentTripUri);
                    con.startActivity(intentViewTrip);
                }
            });

        }

        private void showDeleteConfirmationDialog(final Uri trip) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(con);
            builder.setMessage(R.string.delete_dialog_msg);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the trip.
                    deleteTrip(trip);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the trip.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        /**
         * Perform the deletion of the trip in the database.
         */
        private void deleteTrip(Uri delTrip) {
            // Only perform the delete if this is an existing trip.
            if (delTrip != null) {
                // Call the ContentResolver to delete the trip at the given content URI.
                // Pass in null for the selection and selection args because the mCurrentTripUri
                // content URI already identifies the trip that we want.
                int rowsDeleted = getContentResolver().delete(delTrip, null, null);
                // Show a toast message depending on whether or not the delete was successful.
                if (rowsDeleted == 0) {
                    // If no rows were deleted, then there was an error with the delete.
                    Toast.makeText(con, getString(R.string.delete_trip_fail),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the delete was successful and we can display a toast.
                    Toast.makeText(con, getString(R.string.delete_trip_success),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }

    }


}
