package com.udev.ordinaryweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Created by Vic on 13/9/23.
 * Lists the weather data category options (Currently, Minutely, Hourly, Daily)
 */
public class ListDataActivity extends FragmentActivity
        implements ListDataFragment.Callbacks {

    private static final String TAG = "ListDataActivity";
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        Log.i(TAG, getIntent().getStringExtra("data"));

        if (findViewById(R.id.data_detail_container) != null) {
            mTwoPane = true;

            ((ListDataFragment)getSupportFragmentManager()
                .findFragmentById(R.id.data_list))
                .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(DisplayWeatherFragment.ARG_ITEM_ID, id);
            DisplayWeatherFragment fragment = new DisplayWeatherFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.data_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, DisplayWeatherActivity.class);
            detailIntent.putExtra(DisplayWeatherFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
