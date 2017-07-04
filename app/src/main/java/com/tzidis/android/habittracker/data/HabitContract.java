package com.tzidis.android.habittracker.data;

import android.provider.BaseColumns;

public final class HabitContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private HabitContract() {}

    /* Inner class that defines the table contents */
    public static class HabitEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "habits";
        // The unique id
        public static final String _ID = BaseColumns._ID;
        // Name Column
        public static final String COLUMN_HABIT_NAME = "name";
        // Type Column
        public static final String COLUMN_HABIT_TYPE = "type";
        // Date Column
        public static final String COLUMN_HABIT_DATE = "date";

        /**
         * Possible values for the type of the habit.
         */
        public static final int TYPE_POSITIVE = 0;
        public static final int TYPE_NEGATIVE = 1;

    }
}