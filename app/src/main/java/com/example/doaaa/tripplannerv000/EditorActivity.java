package com.example.doaaa.tripplannerv000;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.doaaa.tripplannerv000.TripContract.TripEntry;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;


public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_TRIP_LOADER = 0;

    private Uri mCurrentTripUri;

    private EditText mNameEditText;

    private EditText sPointEditText;

    private EditText ePointEditText;

    private EditText timeEditText;

    private EditText notesEditText;

    private Spinner typeSpinner;

    private Button addNotesButton;

    private int mType= TripEntry.TYPE_UNKNOWN;

    private boolean mTripHasChanged = false;

    TimePickerDialog timePickerDialog;
    Calendar calendar;
    int currentHour;
    int currentMinute;
    String amPm;
    String notesBuilder= "";
    Intent fromIntent;
    Intent toIntent;
    static int fromCode = 1;
    static int toCode = 0;
    String TAG = "Maradona10";
    private Button startTheTrip;
    com.google.android.gms.maps.model.LatLng start;
    LatLng end;



    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTripHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new trip or editing an existing one.
        Intent intent = getIntent();
        mCurrentTripUri = intent.getData();

        // If the intent DOES NOT contain a trip content URI, then we know that we are
        // creating a new trip.
        if (mCurrentTripUri == null) {
            // This is a new trip, so change the app bar to say "Add a Trip"
            setTitle(getString(R.string.editor_activity_title_new_trip));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a trip that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing trip, so change app bar to say "Edit Trip"
            setTitle(getString(R.string.editor_activity_title_edit_trip));

            // Initialize a loader to read the trip data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_TRIP_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_trip_name);
        sPointEditText=(EditText)findViewById(R.id.edit_sPoint);
        ePointEditText=(EditText)findViewById(R.id.edit_ePoint);
        timeEditText=(EditText)findViewById(R.id.edit_time);
        notesEditText=(EditText) findViewById(R.id.edit_notes);
        addNotesButton=(Button) findViewById(R.id.addNoteBtn);
        typeSpinner=(Spinner) findViewById(R.id.spinner_type);


        mNameEditText.setOnTouchListener(mTouchListener);
        sPointEditText.setOnTouchListener(mTouchListener);
        ePointEditText.setOnTouchListener(mTouchListener);
        timeEditText.setOnTouchListener(mTouchListener);
        typeSpinner.setOnTouchListener(mTouchListener);

        Places.initialize(getApplicationContext(),"AIzaSyALIxKzCB4HCfZyrrOtp7-kgBBkiDBzyYI");

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG);

        fromIntent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        toIntent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        sPointEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(fromIntent, fromCode);
            }
        });

        ePointEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(fromIntent, toCode);
            }
        });

        addNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               notesBuilder= notesBuilder+notesEditText.getText().toString()+"$";
               notesEditText.setText("");
                Toast.makeText(EditorActivity.this, "Note added Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        timeEditText.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    calendar = Calendar.getInstance();
                                                    currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                                                    currentMinute = calendar.get(Calendar.MINUTE);
                                                }

                                                timePickerDialog = new TimePickerDialog(EditorActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                                    @Override
                                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                                                        if (hourOfDay >= 12) {
                                                            amPm = "PM";
                                                        } else {
                                                            amPm = "AM";
                                                        }
                                                        timeEditText.setText(String.format("%02d:%02d", hourOfDay, minutes) +" "+ amPm);
                                                    }
                                                }, currentHour, currentMinute, false);

                                                timePickerDialog.show();
                                            }
                                        }

        );

        setupSpinner();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == fromCode) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                start=place.getLatLng();
                sPointEditText.setText(place.getName());
            }
            else if(requestCode==toCode)
            {
                Place place = Autocomplete.getPlaceFromIntent(data);
                end=place.getLatLng();
                ePointEditText.setText(place.getName());
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the trip.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_trip_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        typeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_one_direction))) {
                        mType = TripEntry.ONE_DIRECTION;
                    } else if (selection.equals(getString(R.string.type_round_trip))) {
                        mType = TripEntry.ROUND_TRIP;
                    } else {
                        mType = TripEntry.TYPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = TripEntry.TYPE_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save trip into database.
     */
    private void saveTrip() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String sPointString=sPointEditText.getText().toString().trim();
        String ePointString=ePointEditText.getText().toString().trim();
        String timeString=timeEditText.getText().toString().trim();
        String notesString=notesBuilder;


        // Check if this is supposed to be a new trip
        // and check if all the fields in the editor are blank
        if (mCurrentTripUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(sPointString) &&
                TextUtils.isEmpty(ePointString) && mType == TripEntry.TYPE_UNKNOWN &&
                TextUtils.isEmpty(timeString) ) {
            // Since no fields were modified, we can return early without creating a new trip.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and trip attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(TripEntry.COLUMN_TRIP_NAME, nameString);
        values.put(TripEntry.COLUMN_START_POINT, sPointString);
        values.put(TripEntry.COLUMN_END_POINT, ePointString);
        values.put(TripEntry.COLUMN_TIME, timeString);
        values.put(TripEntry.COLUMN_NOTES, notesString);
        values.put(TripEntry.COLUMN_TRIP_TYPE, mType);

        // Determine if this is a new or existing trip by checking if mCurrentTripUri is null or not
        if (mCurrentTripUri == null) {
            // This is a NEW trip, so insert a new trip into the provider,
            // returning the content URI for the new trip.
            Uri newUri = getContentResolver().insert(TripEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.insert_trip_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.insert_trip_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING trip, so update the trip with content URI: mCurrentTripUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentTripUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentTripUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.update_trip_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_trip_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new trip, hide the "Delete" menu item.
        if (mCurrentTripUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save trip to database
                saveTrip();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the trip hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mTripHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the trip hasn't changed, continue with handling back button press
        if (!mTripHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
                TripEntry.COLUMN_TRIP_TYPE,
                TripEntry.COLUMN_NOTES };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentTripUri,         // Query the content URI for the current trip
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
            int notesColumnIndex = cursor.getColumnIndex(TripEntry.COLUMN_NOTES);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String startPoint = cursor.getString(sPointColumnIndex);
            String endPoint= cursor.getString(ePointColumnIndex);
            String tripTime= cursor.getString(timeColumnIndex);
            int tripType=cursor.getInt(typeColumnIndex);
            notesBuilder=cursor.getString(notesColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            sPointEditText.setText(startPoint);
            ePointEditText.setText(endPoint);
            timeEditText.setText(tripTime);
           // notesEditText.setText(notesBuilder);

            // Type is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is one direction, 2 is round trip).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (tripType) {
                case TripEntry.ONE_DIRECTION:
                    typeSpinner.setSelection(1);
                    break;
                case TripEntry.ROUND_TRIP:
                    typeSpinner.setSelection(2);
                    break;
                default:
                    typeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        sPointEditText.setText("");
        ePointEditText.setText("");
        timeEditText.setText("");
        notesEditText.setText("");
        typeSpinner.setSelection(0); // Select "one direction" trip
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the trip.
                deleteTrip();
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
    private void deleteTrip() {
        // Only perform the delete if this is an existing trip.
        if (mCurrentTripUri != null) {
            // Call the ContentResolver to delete the trip at the given content URI.
            // Pass in null for the selection and selection args because the currentTripUri
            // content URI already identifies the trip that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTripUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_trip_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_trip_success),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }



}
