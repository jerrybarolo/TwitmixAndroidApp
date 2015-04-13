package com.example.android.twitmix.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.twitmix.app.data.TwitmixContract.TwitmixEntry;

/**
 * Manages a local database for twitmix data.
 */
public class TwitmixDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "twitmix.db";

    public TwitmixDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TWITMIX_TABLE = "CREATE TABLE " + TwitmixEntry.TABLE_NAME + " (" +
                TwitmixEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                TwitmixEntry.COLUMN_ID + " TEXT UNIQUE NOT NULL, " +
                TwitmixEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                TwitmixEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                TwitmixEntry.COLUMN_CATEGORY + " TEXT NOT NULL," +
                TwitmixEntry.COLUMN_CONTENT + " TEXT NOT NULL," +
                TwitmixEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                TwitmixEntry.COLUMN_IMAGE + " TEXT NOT NULL, " +
                " UNIQUE (" + TwitmixEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

            sqLiteDatabase.execSQL(SQL_CREATE_TWITMIX_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TwitmixEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
