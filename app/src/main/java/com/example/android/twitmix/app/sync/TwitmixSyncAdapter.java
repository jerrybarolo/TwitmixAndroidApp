package com.example.android.twitmix.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.android.twitmix.app.MainActivity;
import com.example.android.twitmix.app.R;
import com.example.android.twitmix.app.Utility;
import com.example.android.twitmix.app.data.TwitmixContract;

import org.apache.commons.lang3.StringEscapeUtils;
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

/**
 * Created by jerrybarolo on 17/04/15.
 */
public class TwitmixSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = TwitmixSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the data, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final int POST_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_TWITMIX_PROJECTION = new String[] {
        TwitmixContract.TwitmixEntry.COLUMN_TITLE,
        TwitmixContract.TwitmixEntry.COLUMN_CATEGORY,

    };

    // these indices must match the projection
    private static final int INDEX_TWITMIX_TITLE = 0;
    private static final int INDEX_CATEGORY = 1;


    public TwitmixSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

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

                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_ID, postID);
                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_DATE, date);
                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_AUTHOR, authorName);
                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_TITLE, titlePost);
                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_CONTENT, content);
                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_IMAGE, imageName);
                twitmixValues.put(TwitmixContract.TwitmixEntry.COLUMN_CATEGORY, category);
                cVVector.add(twitmixValues);
            }

            // add post to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                int inserted = getContext().getContentResolver().bulkInsert(TwitmixContract.TwitmixEntry.CONTENT_URI, cvArray);

                // Notify if new post is added to database
                if(inserted > 0) {
                    notifyPost();
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting Sync.");

        String categoryQuery = Utility.getPreferredCategory(getContext());;
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
                    .appendQueryParameter(CATEGORY_PARAM, categoryQuery)
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
                return;
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
                return;
            }

            twitmixJsonStr = buffer.toString();

            String withCharacters = StringEscapeUtils.unescapeHtml4(twitmixJsonStr);
            String noHTMLString = withCharacters.replaceAll("\\<.*?>","");

            getPostDataFromJson(noHTMLString , categoryQuery);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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
        return;
    }


    /**
    +     * Helper method to schedule the sync adapter periodic execution
    +     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                        syncPeriodic(syncInterval, flexTime).
                        setSyncAdapter(account, authority).
                        setExtras(new Bundle()).build();

            ContentResolver.requestSync(request);
        }
        else {
        ContentResolver.addPeriodicSync(account,
                        authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
        * Since we've created an account
        */
        TwitmixSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
        * Without calling setSyncAutomatically, our periodic sync will not be enabled.
        */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
        * Finally, let's do a sync to get things started
        */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyPost() {
        Context context = getContext();

        String categoryQuery = Utility.getPreferredCategory(context);

        // Define the text of the forecast.
        String contentText = "New Post in " + categoryQuery;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                .setContentTitle("TwitMix Notify : ")
                .setContentText(contentText);

        // Make something interesting happen when the user clicks on the notification.
        // In this case, opening the app is sufficient.
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(
        0,
        PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // POST_NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(POST_NOTIFICATION_ID, mBuilder.build());
    }
}
