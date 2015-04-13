package com.example.android.twitmix.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String POSTFRAGMENT_TAG = "PFTAG";

    private String mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCategory = Utility.getPreferredCategory(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PostFragment(), POSTFRAGMENT_TAG)
                    .commit();
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
            PostFragment pf = (PostFragment)getSupportFragmentManager().findFragmentByTag(POSTFRAGMENT_TAG);
            if ( null != pf ) {
                pf.onCategoryChanged();
            }
            mCategory = category;
        }
    }
}
