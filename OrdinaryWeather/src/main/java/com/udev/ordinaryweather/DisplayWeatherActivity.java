package com.udev.ordinaryweather;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class DisplayWeatherActivity extends Activity {

    public void requestWeatherData() {
        if (!mBound) {
            Log.i(TAG, "bind to RequestDataService");
            return;
        }

        Log.e(TAG, "Let's go back in time!");
        Log.i(TAG, "requestWeatherData sending Message.what = " + String.valueOf(RequestDataService.REQUEST_DATA));
        Message msg = Message.obtain(null, RequestDataService.REQUEST_DATA, 0, 0);

        // i wonder if this is an example of time travel, a misunderstanding i have in how the operations take place via
        // threading or otherwise, OR is the Log class itself have a queue nevermind because it should still be
        // in order...thhis is fucked up i no longer know how to cpmputer WTF
        // - Adam

        /*
        well... my head asplode... -Victor (may he rest in peace)
         */
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
//            IBinder binder = service;
//            mService = binder.getService();
            mMessenger = new Messenger(service);
            mBound = true;

            requestWeatherData();//todo:travel back in time
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    private static final String TAG = "DisplayWeatherActivity";

    private RequestDataService mService;
    private boolean mBound = false;
    private Messenger mMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        Button button = (Button)findViewById(R.id.refresh_button);
        try {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestWeatherData();
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, RequestDataService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading_screen, menu);
        return true;
    }
}