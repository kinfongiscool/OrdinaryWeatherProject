package com.udev.ordinaryweather;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;

public class LoadingScreenActivity extends Activity {

    private static final String TAG = "LoadingScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        if (savedInstanceState == null) {
            Intent intent = new Intent("ACTION_WEATHER");
            ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    
                }
    
                @Override
                public void onServiceDisconnected(ComponentName componentName) {
    
                }
            };

            if(!bindService(intent, serviceConnection, BIND_NOT_FOREGROUND)) {
                Log.e(TAG, "Failed to bind to existing service");

            }
        } else {
            Log.i(TAG, "Success!");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading_screen, menu);
        return true;
    }
    
}
