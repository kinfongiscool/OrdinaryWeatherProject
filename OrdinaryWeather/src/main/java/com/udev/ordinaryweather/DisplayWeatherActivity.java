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

        Message msg = Message.obtain(null, RequestDataService.REQUEST_DATA, 0, 0);

        try {
            mServiceMessenger.send(msg);
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
            mServiceMessenger = new Messenger(service);
            mBound = true;

            //force the service to update the data once
            requestWeatherData();
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    private static final String TAG = "DisplayWeatherActivity";

    private boolean mBound = false;
    private Messenger mServiceMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

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