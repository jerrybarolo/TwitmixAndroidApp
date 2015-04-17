package com.example.android.twitmix.app;

/**
 * Created by jerrybarolo on 06/04/15.
 */

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.twitmix.app.data.TwitmixContract;
import com.example.android.twitmix.app.sync.TwitmixSyncAdapter;

/**
 * Encapsulates fetching the data and displaying it as a {@link ListView} layout.
 */
public class PostFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private TwitmixAdapter mTwitmixAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseLastPostLayout = true;

    private static final String SELECTED_KEY = "selected_position";

    private static final int TWITMIX_LOADER = 0;

    private static final String[] TWITMIX_COLUMNS = {
            TwitmixContract.TwitmixEntry.TABLE_NAME + "." + TwitmixContract.TwitmixEntry._ID,
            TwitmixContract.TwitmixEntry.COLUMN_ID,
            TwitmixContract.TwitmixEntry.COLUMN_TITLE,
            TwitmixContract.TwitmixEntry.COLUMN_AUTHOR,
            TwitmixContract.TwitmixEntry.COLUMN_DATE,
            TwitmixContract.TwitmixEntry.COLUMN_CATEGORY,
            TwitmixContract.TwitmixEntry.COLUMN_IMAGE,
            TwitmixContract.TwitmixEntry.COLUMN_CONTENT
    };

    // These indices are tied to TWITMIX_COLUMNS.  If TWITMIX_COLUMNS changes, these
    // must change.
    static final int COL_TWITMIX_AUTO_ID = 0;
    static final int COL_TWITMIX_ID = 1;
    static final int COL_TWITMIX_TITLE = 2;
    static final int COL_TWITMIX_AUTHOR = 3;
    static final int COL_TWITMIX_DATE = 4;
    static final int COL_TWITMIX_CATEGORY_SETTING = 5;
    static final int COL_TWITMIX_IMAGE = 6;
    static final int COL_TWITMIX_CONTENT = 7;

    /**
      * A callback interface that all activities containing this fragment must
      * implement. This mechanism allows activities to be notified of item
      * selections.
      */
    public interface Callback {
        /**
          * DetailFragmentCallback for when an item has been selected.
          */
        public void onItemSelected(Uri postUri);
    }

    public PostFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.postfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updatePost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The TwitmixAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mTwitmixAdapter = new TwitmixAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_post);
        mListView.setAdapter(mTwitmixAdapter);

        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String categorySetting = Utility.getPreferredCategory(getActivity());
                    //String postId = cursor.getString(COL_TWITMIX_ID);
                    //Intent intent = new Intent(getActivity(), DetailActivity.class)
                            //.setData(TwitmixContract.TwitmixEntry.buildTwitmixCategoryWithId(categorySetting, postId));
                    ((Callback) getActivity())
                            .onItemSelected(TwitmixContract.TwitmixEntry
                                    .buildTwitmixCategoryWithId(categorySetting, cursor.getString(COL_TWITMIX_ID)));

                    //startActivity(intent);
                }

                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
           // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mTwitmixAdapter.setUseLastPostLayout(mUseLastPostLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TWITMIX_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the category when we create the loader, all we need to do is restart things
    void onCategoryChanged( ) {
        updatePost();
        getLoaderManager().restartLoader(TWITMIX_LOADER, null, this);
    }

    private void updatePost() {
        //FetchTwitmixTask postTask = new FetchTwitmixTask(getActivity());
        //String category = Utility.getPreferredCategory(getActivity());
        //postTask.execute(category);

        TwitmixSyncAdapter.syncImmediately(getActivity());
    }

    @Override
        public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String categorySetting = Utility.getPreferredCategory(getActivity());

        Uri twitmixForCategoryUri = TwitmixContract.TwitmixEntry.buildTwitmixCategory(categorySetting);

        return new CursorLoader(getActivity(),
                twitmixForCategoryUri,
                TWITMIX_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mTwitmixAdapter.swapCursor(cursor);

        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTwitmixAdapter.swapCursor(null);
    }

    public void setUseLastPostLayout(boolean useLastPostLayout) {
        mUseLastPostLayout = useLastPostLayout;
        if (mTwitmixAdapter != null) {
            mTwitmixAdapter.setUseLastPostLayout(mUseLastPostLayout);
            }
    }
}
