package com.example.android.twitmix.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the twitmix database.
 */
public class TwitmixContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.twitmix.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TWITMIX = "twitmix";

    /* Inner class that defines the table contents of the twitmix table */
    public static final class TwitmixEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TWITMIX).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TWITMIX;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TWITMIX;

        public static final String TABLE_NAME = "twitmix";

        public static final String COLUMN_ID = "ID";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_IMAGE = "image";

        public static Uri buildTwitmixUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTwitmixCategory(String categorySetting) {
            return CONTENT_URI.buildUpon().appendPath(categorySetting).build();
        }

        public static Uri buildTwitmixCategoryWithId(String categorySetting, String postId) {
            return CONTENT_URI.buildUpon().appendPath(categorySetting)
                    .appendPath(postId).build();
        }

        public static Uri buildTwitmixWithCategory(String categorySetting) {
            return CONTENT_URI.buildUpon().appendPath(categorySetting)
                    .appendQueryParameter(COLUMN_CATEGORY, categorySetting).build();
        }

        public static String getCategorySettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}
