package com.udev.ordinaryweather;

import android.app.Activity;
import android.app.FragmentManager;
import android.view.Menu;

public class DisplayWeatherActivity extends Activity
        implements FragmentManager.OnBackStackChangedListener {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading_screen, menu);
        return true;
    }

    @Override
    public void onBackStackChanged() {
        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }
}