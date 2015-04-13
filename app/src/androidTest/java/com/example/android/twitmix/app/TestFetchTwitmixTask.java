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
 *//*

package com.example.android.twitmix.app;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.android.twitmix.app.data.TwitmixContract;

public class TestFetchTwitmixTask extends AndroidTestCase{
    static final String ADD_TWITMIX_CATEGORY_SETTING = "news";
    static final String ADD_TWITMIX_TITLE = "Post";
    static final String ADD_TWITMIX_AUTHOR = "Gerardo Fiorletta";
    static final String ADD_TWITMIX_DATE = "25/03/2015";

    @TargetApi(11)
    public void testAddTwitmix() {
        // start from a clean state
        getContext().getContentResolver().delete(TwitmixContract.TwitmixEntry.CONTENT_URI,
                TwitmixContract.TwitmixEntry.COLUMN_CATEGORY + " = ?",
                new String[]{ADD_TWITMIX_CATEGORY_SETTING});

        FetchTwitmixTask fwt = new FetchTwitmixTask(getContext());

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            Cursor twitmixCursor = getContext().getContentResolver().query(
                    TwitmixContract.TwitmixEntry.CONTENT_URI,
                    new String[]{
                            TwitmixContract.TwitmixEntry._ID,
                            TwitmixContract.TwitmixEntry.COLUMN_CATEGORY,
                            TwitmixContract.TwitmixEntry.COLUMN_TITLE,
                            TwitmixContract.TwitmixEntry.COLUMN_AUTHOR,
                            TwitmixContract.TwitmixEntry.COLUMN_DATE
                    },
                    TwitmixContract.TwitmixEntry.COLUMN_CATEGORY + " = ?",
                    new String[]{ADD_TWITMIX_CATEGORY_SETTING},
                    null);

            // these match the indices of the projection
            if (twitmixCursor.moveToFirst()) {
                assertEquals("Error: the queried value of locationId does not match the returned value" +
                        "from", twitmixCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_CITY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longitude is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            // add the location again
            long newLocationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                    ADD_LOCATION_LAT, ADD_LOCATION_LON);

            assertEquals("Error: inserting a location again should return the same ID",
                    locationId, newLocationId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(WeatherContract.LocationEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
*/
