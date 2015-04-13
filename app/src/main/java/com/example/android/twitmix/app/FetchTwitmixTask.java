package com.example.android.twitmix.app;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.twitmix.app.data.TwitmixContract.TwitmixEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class FetchTwitmixTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchTwitmixTask.class.getSimpleName();
    private final Context mContext;

    public FetchTwitmixTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    /**
     * Take the String representing the complete data in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getPostDataFromJson(String postJsonStr, String category)
            throws JSONException {

        final String OWM_POST_ID = "ID";

        final String OWM_TITLE = "title";

        final String OWM_AUTHOR = "author";
        final String OWM_AUTHOR_NAME = "name"; // children of author

        final String OWM_CONTENT = "content";
        final String OWM_DATE = "date";

        final String OWM_IMAGE = "featured_image";
        final String OWM_LINK_IMAGE = "guid"; // children of featured_image

        try{

        JSONArray postJsonArray = new JSONArray(postJsonStr);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(postJsonArray.length());

        for (int i = 0; i < postJsonArray.length(); i++) {
            JSONObject post = postJsonArray.getJSONObject(i);
            String postID = post.getString(OWM_POST_ID);
            String titlePost = post.getString(OWM_TITLE);
            String content = post.getString(OWM_CONTENT);
            String date = post.getString(OWM_DATE);

            JSONObject authorJson = post.getJSONObject(OWM_AUTHOR);
            String authorName = authorJson.getString(OWM_AUTHOR_NAME);

            JSONObject imageJson = post.getJSONObject(OWM_IMAGE);
            String imageName = imageJson.getString(OWM_LINK_IMAGE);

            ContentValues twitmixValues = new ContentValues();

            twitmixValues.put(TwitmixEntry.COLUMN_ID, postID);
            twitmixValues.put(TwitmixEntry.COLUMN_DATE, date);
            twitmixValues.put(TwitmixEntry.COLUMN_AUTHOR, authorName);
            twitmixValues.put(TwitmixEntry.COLUMN_TITLE, titlePost);
            twitmixValues.put(TwitmixEntry.COLUMN_CONTENT, content);
            twitmixValues.put(TwitmixEntry.COLUMN_IMAGE, imageName);
            twitmixValues.put(TwitmixEntry.COLUMN_CATEGORY, category);
            cVVector.add(twitmixValues);
            }

            // add post to database
            int inserted = 0;
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(TwitmixEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchTwitmixTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        String categoryQuery = params[0];
        Log.v(LOG_TAG, "Connesso al URI categoryQuery = " + categoryQuery);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String twitmixJsonStr = null;

        int postsPage = 10;

        try {
            // Construct the URL for the twitmix query
            final String POST_BASE_URL =
                    "http://www.twitmix.it/wp-json/posts?";

            final String CATEGORY_PARAM = "filter[category_name]";
            final String NUM_POSTS_PAGE_PARAM = "filter[posts_per_page]";

            Uri builtUri = Uri.parse(POST_BASE_URL).buildUpon()
                    .appendQueryParameter(CATEGORY_PARAM, params[0])
                    .appendQueryParameter(NUM_POSTS_PAGE_PARAM, Integer.toString(postsPage))
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request to twitmix, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.v(LOG_TAG, "Connesso al URI ");

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            Log.v(LOG_TAG, "BufferedReader ");
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            twitmixJsonStr = buffer.toString();
            getPostDataFromJson(twitmixJsonStr , categoryQuery);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }
}
