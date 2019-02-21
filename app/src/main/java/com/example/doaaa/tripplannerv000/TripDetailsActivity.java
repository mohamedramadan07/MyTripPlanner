package com.example.doaaa.tripplannerv000;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.doaaa.tripplannerv000.TripContract.TripEntry;


public class TripDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_TRIP_LOADER = 0;
    Uri currentTripUri;
    TextView nameTextView ;
    TextView sPointTextView ;
    TextView ePointTextView ;
    TextView timeTextView ;
    TextView typeTextView;
    TextView notesTextView;
    ListView notesList;
    Button notesButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent=getIntent();
        currentTripUri=intent.getData();


        if(currentTripUri!=null){
            // Initialize a loader to read the trip data from the database
            // and display the current values in the viewer
            getLoaderManager().initLoader(EXISTING_TRIP_LOADER, null, this);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(TripDetailsActivity.this, EditorActivity.class);
                i.setData(currentTripUri);
                startActivity(i);
            }
        });

        // Find fields to populate in inflated template
         nameTextView = (TextView) findViewById(R.id.showTripName);
         sPointTextView = (TextView) findViewById(R.id.showStartPoint);
         ePointTextView=(TextView) findViewById(R.id.showEndPoint);
         timeTextView =(TextView) findViewById(R.id.showTripTime);
         typeTextView=(TextView) findViewById(R.id.showTripType);
         notesTextView=(TextView) findViewById(R.id.showTripNotes);
         notesButton=(Button) findViewById(R.id.notesBtn);
         notesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i= new Intent(TripDetailsActivity.this, TripNotesActivity.class);
                 i.setData(currentTripUri);
                 startActivity(i);
             }
         });

    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all trip attributes, define a projection that contains
        // all columns from the trip table
        String[] projection = {
                TripEntry._ID,
                TripEntry.COLUMN_TRIP_NAME,
                TripEntry.COLUMN_START_POINT,
                TripEntry.COLUMN_END_POINT,
                TripEntry.COLUMN_TIME,
                TripEntry.COLUMN_TRIP_TYPE };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentTripUri,         // Query the content URI for the current trip
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of trip attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_TRIP_NAME);
            int sPointColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_START_POINT);
            int ePointColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_END_POINT);
            int timeColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_TIME);
            int typeColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_TRIP_TYPE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String startPoint = cursor.getString(sPointColumnIndex);
            String endPoint= cursor.getString(ePointColumnIndex);
            String tripTime= cursor.getString(timeColumnIndex);
            int tripType=cursor.getInt(typeColumnIndex);


            // Update the views on the screen with the values from the database
            nameTextView.setText(name);
            sPointTextView.setText(startPoint);
            ePointTextView.setText(endPoint);
            timeTextView.setText(tripTime);


            switch (tripType) {
                case TripEntry.ONE_DIRECTION:
                    typeTextView.setText("One Direction Trip");
                    break;
                case TripEntry.ROUND_TRIP:
                    typeTextView.setText("Round Trip");
                    break;
                default:
                    typeTextView.setText("Unknown Trip");
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the fields.
        nameTextView.setText("");
        sPointTextView.setText("");
        ePointTextView.setText("");
        timeTextView.setText("");
        notesTextView.setText("");
        typeTextView.setText("Unknown Trip");
    }


}
