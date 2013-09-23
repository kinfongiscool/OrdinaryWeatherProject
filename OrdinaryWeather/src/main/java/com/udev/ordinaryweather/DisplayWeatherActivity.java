package com.udev.ordinaryweather;

import android.app.Activity;
import android.app.FragmentManager;
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
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class DisplayWeatherActivity extends Activity
        implements FragmentManager.OnBackStackChangedListener {

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

    private static void updateDisplay(Activity activity) {
        try {
            JSONObject currentForecast = mData.getJSONObject("currently");
            Date formattedTime = new Date();

            String[] conditions = new String[]{
                    "Date: " + formattedTime.toString(),
                    stringValueForKey(currentForecast, "summary", "Summary"),
                    stringValueForKey(currentForecast, "precipType", "Precipitation Type"),
                    stringValueForKey(currentForecast, "temperature", "Temperature"),
                    stringValueForKey(currentForecast, "apparentTemperature", "Feels like"),
                    stringValueForKey(currentForecast, "dewPoint", "Dew Point"),
                    stringValueForKey(currentForecast, "windSpeed", "Wind Speed"),
                    stringValueForKey(currentForecast, "windBearing", "Wind Bearing"),
                    stringValueForKey(currentForecast, "cloudCover", "Cloud Cover"),
                    stringValueForKey(currentForecast, "humidity", "Humidity"),
                    stringValueForKey(currentForecast, "pressure", "Pressure"),
                    stringValueForKey(currentForecast, "visibility", "Visibility"),
                    stringValueForKey(currentForecast, "ozone", "Ozone")
            };

            ArrayAdapter adapter = new ArrayAdapter<String>(activity.getApplicationContext(), R.layout.weather_info, conditions);
            ListView listView = (ListView)activity.findViewById(R.id.weather_info_list);
            listView.setAdapter(adapter);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    public class DataBroadcastReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "DataBroadcastReceiver.onReceive " + intent.getStringExtra("data"));
//            try {
//                mData = new JSONObject(intent.getStringExtra("data"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            getFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new DisplayWeatherFragment())
//                    .commit();
//        }
//    }

    private static final String TAG = "DisplayWeatherActivity";

    private boolean mBound = false;
    private Messenger mServiceMessenger;
//    private DataBroadcastReceiver dataBroadcastReceiver;
    private static JSONObject mData;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activty_fragment_container);
//
//        dataBroadcastReceiver = new DataBroadcastReceiver();
//        registerReceiver(dataBroadcastReceiver, new IntentFilter("android.intent.action.ACTION_DISPLAY_FORECAST"));
//
//        getFragmentManager()
//                .beginTransaction()
//                .add(R.id.fragment_container, new LoadingFragment())
//                .addToBackStack(null)
//                .commit();
//    }

    @Override
    protected void onStart() {
//        super.onStart();
//        Intent intent = new Intent(this, RequestDataService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
//        super.onStop();
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
    }

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

    private static String stringValueForKey(JSONObject obj, String key) {
        try {
            return obj.getString(key);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return "None";
    }

    private static String stringValueForKey(JSONObject obj, String key, String label) {

        return label + ": " + stringValueForKey(obj, key);
    }
}