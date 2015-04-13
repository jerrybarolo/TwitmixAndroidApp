package com.example.android.twitmix.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.twitmix.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;


public class TestUtilities extends AndroidTestCase {
    static final String TEST_CATEGORY = "news";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createTwitmixValues() {
        ContentValues twitmixValues = new ContentValues();
        twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_CATEGORY, TEST_CATEGORY);
        twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_DATE, "21/03/2015");
        twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_AUTHOR, "Antonella Corsetti");
        twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_CONTENT, "Ciao a tutti ecco il twitmix project");
        twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_TITLE, "Gianna Chillà");
        twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_IMAGE, "image.png");

        return twitmixValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        LocationEntry part of the TwitmixContract as well as the TwitmixDbHelper.
     */
    static long insertTwitmixValues(Context context) {
        // insert our test records into the database
        TwitmixDbHelper dbHelper = new TwitmixDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createTwitmixValues();

        long twitmixRowId;
        twitmixRowId = db.insert(TwitmixContract.TwitmixEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Twitmix Values", twitmixRowId != -1);

        return twitmixRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
