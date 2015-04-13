/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.twitmix.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(TwitmixDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(TwitmixContract.TwitmixEntry.TABLE_NAME);

        mContext.deleteDatabase(TwitmixDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new TwitmixDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );
        assertTrue("Error: Your database was created without the entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + TwitmixContract.TwitmixEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> twitmixColumnHashSet = new HashSet<String>();
        twitmixColumnHashSet.add(TwitmixContract.TwitmixEntry._ID);
        twitmixColumnHashSet.add(TwitmixContract.TwitmixEntry.COLUMN_AUTHOR);
        twitmixColumnHashSet.add(TwitmixContract.TwitmixEntry.COLUMN_DATE);
        twitmixColumnHashSet.add(TwitmixContract.TwitmixEntry.COLUMN_TITLE);
        twitmixColumnHashSet.add(TwitmixContract.TwitmixEntry.COLUMN_CONTENT);
        twitmixColumnHashSet.add(TwitmixContract.TwitmixEntry.COLUMN_IMAGE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            twitmixColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                twitmixColumnHashSet.isEmpty());
        db.close();
    }


    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testTwitmixTable() {

        // First step: Get reference to writable database
        SQLiteDatabase db = new TwitmixDbHelper(this.mContext).getWritableDatabase();

        ContentValues testValues = TestUtilities.createTwitmixValues();

        long rowId = db.insert(TwitmixContract.TwitmixEntry.TABLE_NAME, null, testValues);

        assert(rowId != -1);

        Cursor cursor = db.query(
                TwitmixContract.TwitmixEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
                );

        assertTrue("Error: No records returned", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: Validation failed," , cursor, testValues);

        assertFalse("Error: More than one record" , cursor.moveToNext());

        cursor.close();
        db.close();
    }
}
