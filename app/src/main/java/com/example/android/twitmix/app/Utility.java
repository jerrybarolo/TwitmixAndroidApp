package com.example.android.twitmix.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jerrybarolo on 12/04/15.
 */

public class Utility {
    public static String getPreferredCategory(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_category_key),
                context.getString(R.string.pref_category_news));
    }
}
