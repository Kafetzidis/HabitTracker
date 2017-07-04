package com.tzidis.android.habittracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tzidis.android.habittracker.data.HabitDbHelper;
import com.tzidis.android.habittracker.data.HabitContract.HabitEntry;

import java.util.Calendar;

public class HabitActivity extends AppCompatActivity {

    private HabitDbHelper mDbHelper;

    /** EditText field to enter the habit's name */
    private EditText mNameEditText;

    /** EditText field to enter the habit's type */
    private Spinner mHabitTypeSpinner;

    /**
     * Type of the habit. The possible values are:
     * 0 for good habit, 1 for bad habit.
     */
    private int mHabitType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit);

        mDbHelper = new HabitDbHelper(this);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_habit_name);
        mHabitTypeSpinner = (Spinner) findViewById(R.id.spinner_habit);

        //Find all relevant buttons
        /*Button for inserting habit*/
        Button mInsertButton = (Button) findViewById(R.id.save_button);
        /*Button to show database*/
        Button mShowDbButton = (Button) findViewById(R.id.show_button);

        setupSpinner();

        mInsertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertHabit();
                mNameEditText.getText().clear();
                setupSpinner();
            }
        });

        mShowDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResults();
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the habit.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter habitSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_habit_type, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        habitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mHabitTypeSpinner.setAdapter(habitSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mHabitTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.spinner_1))) {
                        mHabitType = HabitEntry.TYPE_POSITIVE; // Good
                    } else if (selection.equals(getString(R.string.spinner_2))) {
                        mHabitType = HabitEntry.TYPE_NEGATIVE; // Bad
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mHabitType = 0; // Unknown
            }
        });
    }

    //Gets user input data from editor and inserts them into the database
    private void insertHabit() {

        mDbHelper = new HabitDbHelper(this);

        //Find the relevant editTexts and get user input
        String nameString = mNameEditText.getText().toString().trim();

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        if (nameString != null && !nameString.isEmpty()) {

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(HabitEntry.COLUMN_HABIT_NAME, nameString);
            values.put(HabitEntry.COLUMN_HABIT_TYPE, mHabitType);


            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(HabitEntry.TABLE_NAME, null, values);

            Log.v("EditorActivity", "New Row ID: " + newRowId);

            //Show a toast message depending on whether or not the insertion was successful
            if (newRowId == -1) {
                Toast.makeText(this, "Error with saving habit", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Habit saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Habit name is empty!", Toast.LENGTH_SHORT).show();
        }

    }

    private Cursor readDatabase() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        HabitDbHelper mDbHelper = new HabitDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                HabitEntry._ID,
                HabitEntry.COLUMN_HABIT_NAME,
                HabitEntry.COLUMN_HABIT_TYPE,
                HabitEntry.COLUMN_HABIT_DATE,
        };

        Cursor cursor = db.query(
                HabitEntry.TABLE_NAME,                    // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        return cursor;
    }

    private void showResults(){

        Cursor cursor = readDatabase();
        TextView displayView = (TextView) findViewById(R.id.db_textview);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The habits table contains <number of rows in Cursor> habits.
            // _id - name - type - date
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText("The habits table contains " + cursor.getCount() + " habits.\n\n");
            displayView.append(HabitEntry._ID + " - " +
                    HabitEntry.COLUMN_HABIT_NAME + " - " +
                    HabitEntry.COLUMN_HABIT_TYPE + " - " +
                    HabitEntry.COLUMN_HABIT_DATE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(HabitEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(HabitEntry.COLUMN_HABIT_NAME);
            int typeColumnIndex = cursor.getColumnIndex(HabitEntry.COLUMN_HABIT_TYPE);
            int dateColumnIndex = cursor.getColumnIndex(HabitEntry.COLUMN_HABIT_DATE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentType = cursor.getInt(typeColumnIndex);
                String currentDate = cursor.getString(dateColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentType + " - " +
                        currentDate));
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

}
