package com.example.doaaa.tripplannerv000;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.doaaa.tripplannerv000.TripContract.TripEntry;

import java.util.ArrayList;

public class TripNotesActivity extends AppCompatActivity  implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_TRIP_LOADER = 0;
    Uri currentTripUri;
    ListView notesList;
    Button addNoteBtn;
    String tripNotes;
    EditText noteField;
    NotesAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_notes);

        Intent intent = getIntent();
        currentTripUri = intent.getData();
        notesList=(ListView)findViewById(R.id.listNotesActivity);
        addNoteBtn=(Button) findViewById(R.id.addNoteBtnActivity);
        noteField=(EditText)findViewById(R.id.edit_notes_activity);
        if(currentTripUri!=null){
            // Initialize a loader to read the trip data from the database
            // and display the current values in the viewer
            getLoaderManager().initLoader(EXISTING_TRIP_LOADER, null, this);
        }

        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               tripNotes=tripNotes+ noteField.getText().toString()+"$";
                ContentValues values = new ContentValues();
                values.put(TripEntry.COLUMN_NOTES, tripNotes);
                getContentResolver().update(currentTripUri, values, null, null);
                noteField.setText("");
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all trip attributes, define a projection that contains
        // all columns from the trip table
        String[] projection = {
                TripEntry._ID,
                TripEntry.COLUMN_NOTES };

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

            int notesColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_NOTES);

            tripNotes=cursor.getString(notesColumnIndex);

            String[] notesArray=tripNotes.split("\\$");
            adapter=new NotesAdapter(getApplicationContext(), notesArray);
            notesList.setAdapter(adapter);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the fields.
    }



    public class NotesAdapter extends ArrayAdapter<String> {

        ArrayList<String> notesCopy= new ArrayList<>();
        ImageButton deleteNote;
        public NotesAdapter(Context context, String[] notes)  {
            super(context,0,notes );

           for(int i=0; i<notes.length; i++ ){
               notesCopy.add(notes[i]);
           }
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.notes_item, parent, false);

            }

            String currentNote = getItem(position);
            TextView titleView = (TextView) listItemView.findViewById(R.id.noteItemViewForTripDetails);
            titleView.setText(currentNote);
            deleteNote=(ImageButton)listItemView.findViewById(R.id.deleteNoteBtn);
            deleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notesCopy.remove(position);
                    String noteBuild=TextUtils.join("$", notesCopy);
                    tripNotes=noteBuild+"$";
                    ContentValues values = new ContentValues();
                    values.put(TripEntry.COLUMN_NOTES, tripNotes);
                    getContentResolver().update(currentTripUri, values, null, null);
                }
            });

            return  listItemView;
        }
    }
}
