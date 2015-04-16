package com.example.android.twitmix.app;

/**
 * Created by jerrybarolo on 15/04/15.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.twitmix.app.data.TwitmixContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String TWITMIX_SHARE_HASHTAG = " #TwitmixApp";

    private ShareActionProvider mShareActionProvider;
    private String mTwitmixStr;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] TWITMIX_COLUMNS = {
            TwitmixContract.TwitmixEntry.TABLE_NAME + "." + TwitmixContract.TwitmixEntry._ID,
            TwitmixContract.TwitmixEntry.COLUMN_ID,
            TwitmixContract.TwitmixEntry.COLUMN_TITLE,
            TwitmixContract.TwitmixEntry.COLUMN_AUTHOR,
            TwitmixContract.TwitmixEntry.COLUMN_DATE,
            TwitmixContract.TwitmixEntry.COLUMN_IMAGE,
            TwitmixContract.TwitmixEntry.COLUMN_CONTENT
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_TWITMIX_AUTO_ID = 0;
    private static final int COL_TWITMIX_ID = 1;
    private static final int COL_TWITMIX_TITLE = 2;
    private static final int COL_TWITMIX_AUTHOR = 3;
    private static final int COL_TWITMIX_DATE = 4;
    private static final int COL_TWITMIX_IMAGE = 5;
    private static final int COL_TWITMIX_CONTENT = 6;

    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mAuthorView;
    private TextView mContentView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mImageView = (ImageView) rootView.findViewById(R.id.imageView_id);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title_id);
        mAuthorView = (TextView) rootView.findViewById(R.id.detail_author_id);
        mContentView = (TextView) rootView.findViewById(R.id.detail_content_id);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTwitmixIntent());
        }
    }

    private Intent createShareTwitmixIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTwitmixStr + TWITMIX_SHARE_HASHTAG);
        return shareIntent;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    void onCategoryChanged( String newCategory ) {
        // replace the uri, since the category has changed
        Uri uri = mUri;
        if (null != uri) {
                String postId = TwitmixContract.TwitmixEntry.getIdFromUri(uri);
                Uri updatedUri = TwitmixContract.TwitmixEntry.buildTwitmixCategoryWithId(newCategory, postId);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    TWITMIX_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        String imageUrl = data.getString(COL_TWITMIX_IMAGE);
        //Loading image from below url into imageView
        Picasso.with(getActivity())
                .load(imageUrl)
                .into(mImageView);

        String title = data.getString(COL_TWITMIX_TITLE);
        String author = data.getString(COL_TWITMIX_AUTHOR);
        String date = data.getString(COL_TWITMIX_DATE).substring(0,10);
        String content = data.getString(COL_TWITMIX_CONTENT);

        mTitleView.setText(title);
        mAuthorView.setText(author + " " + date);
        mContentView.setText(content);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTwitmixIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
