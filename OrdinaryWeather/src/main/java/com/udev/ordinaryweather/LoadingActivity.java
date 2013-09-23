package com.udev.ordinaryweather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vic on 13/9/23.
 */
public class LoadingActivity extends FragmentActivity {

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

    public class DataBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "DataBroadcastReceiver.onReceive " + intent.getStringExtra("data"));
            try {
                mData = new JSONObject(intent.getStringExtra("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            setContentView(R.layout.activity_list_data);
            Bundle arguments = new Bundle();
            arguments.putString("data", intent.getStringExtra("data"));
            ListDataFragment fragment = new ListDataFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_info_list, fragment)
                    .commit();
        }
    }

    private static final String TAG = "LoadingActivity";

    private boolean mTwoPane = false;
    private boolean mBound = false;
    private Messenger mServiceMessenger;
    private DataBroadcastReceiver dataBroadcastReceiver;
    private static JSONObject mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        dataBroadcastReceiver = new DataBroadcastReceiver();
        registerReceiver(dataBroadcastReceiver, new IntentFilter("android.intent.action.ACTION_DISPLAY_FORECAST"));
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
}
