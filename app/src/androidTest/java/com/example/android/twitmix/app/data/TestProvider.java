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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.twitmix.app.data.TwitmixContract.TwitmixEntry;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                TwitmixEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                TwitmixEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Twitmix table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the TwitmixProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // TwitmixProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                TwitmixProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: TwitmixProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + TwitmixContract.CONTENT_AUTHORITY,
                    providerInfo.authority, TwitmixContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: TwitmixProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(TwitmixEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the TwitmixEntry CONTENT_URI should return TwitmixEntry.CONTENT_TYPE",
                TwitmixEntry.CONTENT_TYPE, type);

        String testCategory = "news";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                TwitmixEntry.buildTwitmixCategory(testCategory));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the TwitmixEntry CONTENT_URI with category should return TwitmixEntry.CONTENT_TYPE",
                TwitmixEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicTwitmixQuery() {
        // insert our test records into the database
        TwitmixDbHelper dbHelper = new TwitmixDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createTwitmixValues();
        TestUtilities.insertTwitmixValues(mContext);

        long rowId = db.insert(TwitmixEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to Insert TwitmixEntry into the Database", rowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor twitmixCursor = mContext.getContentResolver().query(
                TwitmixEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTwitmixQuery", twitmixCursor, testValues);
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update twitmix is functioning correctly.
     */
    public void testUpdateTwitmix() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createTwitmixValues();

        Uri twitmixUri = mContext.getContentResolver().
                insert(TwitmixEntry.CONTENT_URI, values);
        long rowId = ContentUris.parseId(twitmixUri);

        // Verify we got a row back.
        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(TwitmixEntry._ID, rowId);
        updatedValues.put(TwitmixEntry.COLUMN_CONTENT, "Santa's Village");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor twitmixCursor = mContext.getContentResolver().query(TwitmixEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        twitmixCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                TwitmixEntry.CONTENT_URI, updatedValues, TwitmixEntry._ID + "= ?",
                new String[] { Long.toString(rowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        twitmixCursor.unregisterContentObserver(tco);
        twitmixCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                TwitmixEntry.CONTENT_URI,
                null,   // projection
                TwitmixEntry._ID + " = " + rowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    public void testInsertReadProvider() {
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        ContentValues twitmixValues = TestUtilities.createTwitmixValues();

        mContext.getContentResolver().registerContentObserver(TwitmixEntry.CONTENT_URI, true, tco);

        Uri twitmixInsertUri = mContext.getContentResolver().insert(TwitmixEntry.CONTENT_URI, twitmixValues);
        assertTrue(twitmixInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor twitmixCursor = mContext.getContentResolver().query(
                TwitmixEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating TwitmixEntry insert.",
                twitmixCursor, twitmixValues);

        // Get the joined Weather and Location data
        twitmixCursor = mContext.getContentResolver().query(
                TwitmixEntry.buildTwitmixCategory(TestUtilities.TEST_CATEGORY),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating  Twitmix  Category Data.",
                twitmixCursor, twitmixValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver categoryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TwitmixEntry.CONTENT_URI, true, categoryObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        categoryObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(categoryObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertTwitmixValues() {
        String currentTestCategory = TestUtilities.TEST_CATEGORY;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            ContentValues twitmixValues = new ContentValues();
            twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_DATE, "25/08/1978");
            twitmixValues.put(TwitmixEntry.COLUMN_TITLE, "Ciao a tutti");
            twitmixValues.put(TwitmixEntry.COLUMN_AUTHOR, "Antonella Corsetti");
            twitmixValues.put(TwitmixEntry.COLUMN_CONTENT, "Ecco il testo dell'articolo");
            twitmixValues.put(TwitmixEntry.COLUMN_IMAGE, "image.png");
            twitmixValues.put(TwitmixEntry.COLUMN_CATEGORY, "news");
            returnContentValues[i] = twitmixValues;
        }
        return returnContentValues;
    }

    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {
        ContentValues[] bulkInsertContentValues = createBulkInsertTwitmixValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver twitmixObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TwitmixEntry.CONTENT_URI, true, twitmixObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(TwitmixEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        twitmixObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(twitmixObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                TwitmixEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating TwitmixEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
