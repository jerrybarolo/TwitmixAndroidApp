package com.example.android.twitmix.app;

/**
 * Created by jerrybarolo on 12/04/15.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * {@link TwitmixAdapter} exposes a list of twitmix posts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class TwitmixAdapter extends CursorAdapter {

    private final String LOG_TAG = TwitmixAdapter.class.getSimpleName();

    public TwitmixAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        This is ported from FetchTwitmixTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {

        // Put The Image here
        return cursor.getString(PostFragment.COL_TWITMIX_TITLE) +
                " - " + cursor.getString(PostFragment.COL_TWITMIX_AUTHOR) +
                " - " + cursor.getString(PostFragment.COL_TWITMIX_DATE);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_news_post, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
