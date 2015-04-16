package com.example.android.twitmix.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.twitmix.app.data.DetailFragment;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCategory = Utility.getPreferredCategory(this);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.post_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.post_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else
        {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String category = Utility.getPreferredCategory( this );
        // update the location in our second pane using the fragment manager
        if (category != null && !category.equals(mCategory)) {
            PostFragment pf = (PostFragment)getSupportFragmentManager().findFragmentById(R.id.listview_post);
            if ( null != pf ) {
                pf.onCategoryChanged();
            }
            mCategory = category;
        }
    }
}
