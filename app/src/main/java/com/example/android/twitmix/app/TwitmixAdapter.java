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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * {@link TwitmixAdapter} exposes a list of twitmix posts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class TwitmixAdapter extends CursorAdapter {


    public TwitmixAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_LAST_POST = 0;
    private static final int VIEW_TYPE_OLDER_POST = 1;

    /**
     * Cache of the children views for a list item.
     */
    public static class ViewHolder {
        public final ImageView imageView;
        public final TextView authorView;
        public final TextView titleView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.list_item_image);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            authorView = (TextView) view.findViewById(R.id.list_item_author_textview);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_LAST_POST: {
                layoutId = R.layout.list_news_last_post;
                break;
            }
            case VIEW_TYPE_OLDER_POST: {
                layoutId = R.layout.list_news_post;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String imageUrl = cursor.getString(PostFragment.COL_TWITMIX_IMAGE);
        //Loading image from below url into imageView
        Picasso.with(context)
                .load(imageUrl)
                .resize(350, 350)
                .into(viewHolder.imageView);

        viewHolder.titleView.setText(cursor.getString(PostFragment.COL_TWITMIX_TITLE));
        viewHolder.authorView.setText(cursor.getString(PostFragment.COL_TWITMIX_AUTHOR) +
                " - " + cursor.getString(PostFragment.COL_TWITMIX_DATE).substring(0,10));
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_LAST_POST : VIEW_TYPE_OLDER_POST;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
