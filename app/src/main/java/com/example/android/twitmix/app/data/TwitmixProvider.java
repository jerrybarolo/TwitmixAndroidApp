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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TwitmixProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TwitmixDbHelper mOpenHelper;

    static final int TWITMIX = 100;
    static final int TWITMIX_WITH_CATEGORY_AND_ID = 101;
    static final int TWITMIX_WITH_CATEGORY = 300;

    private static final SQLiteQueryBuilder sTwitmixDataByCategorySettingQueryBuilder;

    static{
        sTwitmixDataByCategorySettingQueryBuilder = new SQLiteQueryBuilder();

        sTwitmixDataByCategorySettingQueryBuilder.setTables(TwitmixContract.TwitmixEntry.TABLE_NAME);
    }

    private static final String sCategorySettingSelection =
            TwitmixContract.TwitmixEntry.TABLE_NAME+
                    "." + TwitmixContract.TwitmixEntry.COLUMN_CATEGORY + " = ? ";

    private Cursor getTwitmixDataByCategorySetting(Uri uri, String[] projection, String sortOrder) {
        String categorySetting = TwitmixContract.TwitmixEntry.getCategorySettingFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sCategorySettingSelection;
        selectionArgs = new String[]{categorySetting};

        return sTwitmixDataByCategorySettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static final String sCategoryAndIdSelection =
            TwitmixContract.TwitmixEntry.TABLE_NAME +
                    "." + TwitmixContract.TwitmixEntry.COLUMN_CATEGORY + " = ? AND " +
                    TwitmixContract.TwitmixEntry.COLUMN_ID + " = ? ";

    private Cursor getTwitmixDataByCategoryAndId(Uri uri, String[] projection, String sortOrder) {
        String categorySetting = TwitmixContract.TwitmixEntry.getCategorySettingFromUri(uri);
        String postId = TwitmixContract.TwitmixEntry.getIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sCategoryAndIdSelection;
        selectionArgs = new String[]{categorySetting, postId};

        return sTwitmixDataByCategorySettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TwitmixContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, TwitmixContract.PATH_TWITMIX, TWITMIX);
        matcher.addURI(authority, TwitmixContract.PATH_TWITMIX + "/*", TWITMIX_WITH_CATEGORY);
        matcher.addURI(authority, TwitmixContract.PATH_TWITMIX + "/*/*", TWITMIX_WITH_CATEGORY_AND_ID);
//        matcher.addURI(authority, TwitmixContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
//        matcher.addURI(authority, TwitmixContract.PATH_LOCATION, LOCATION);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new TwitmixDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TWITMIX:
                return TwitmixContract.TwitmixEntry.CONTENT_TYPE;
            case TWITMIX_WITH_CATEGORY:
                return TwitmixContract.TwitmixEntry.CONTENT_TYPE;
            case TWITMIX_WITH_CATEGORY_AND_ID:
                return TwitmixContract.TwitmixEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "twitmix/*"
            case TWITMIX_WITH_CATEGORY: {
                retCursor = getTwitmixDataByCategorySetting(uri, projection, sortOrder);
                break;
            }
            // "twitmix"
            case TWITMIX: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TwitmixContract.TwitmixEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TWITMIX_WITH_CATEGORY_AND_ID: {
                retCursor = getTwitmixDataByCategoryAndId(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TWITMIX: {
                long _id = db.insert(TwitmixContract.TwitmixEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TwitmixContract.TwitmixEntry.buildTwitmixUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case TWITMIX:
                rowsDeleted = db.delete(
                        TwitmixContract.TwitmixEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

/*    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(TwitmixContract.TwitmixEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(TwitmixContract.TwitmixEntry.COLUMN_DATE);
            values.put(TwitmixContract.TwitmixEntry.COLUMN_DATE, TwitmixContract.normalizeDate(dateValue));
        }
    }*/

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case TWITMIX:
                    rowsUpdated = db.update(TwitmixContract.TwitmixEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TWITMIX:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TwitmixContract.TwitmixEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}