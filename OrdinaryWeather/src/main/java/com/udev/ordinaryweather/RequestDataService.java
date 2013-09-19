package com.udev.ordinaryweather;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.http.AndroidHttpClient;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Vic on 13/9/12.
 * Service responsible for fetching data from Forecast.io
 */
public class RequestDataService extends Service implements LocationListener {

    static final int REQUEST_DATA = 1;

    public JSONObject getData() {
        return mData;
    }

    @Override
    public void onLocationChanged(Location location) {
        requestForecastObject(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    removeGpsUpdates();
                    break;
                case LocationProvider.AVAILABLE:
                    break;
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        removeGpsUpdates();
    }

    private void removeGpsUpdates() {
        mLocationManager.removeUpdates(this);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        RequestDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RequestDataService.this;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case RequestDataService.REQUEST_DATA:
                    Log.i(TAG, "ServiceHandler handleMessage: Message.what = " + msg.what);
                    Toast.makeText(getApplicationContext(), "REQUEST_DATA", Toast.LENGTH_SHORT).show();
                    requestSingleGpsUpdate();
                    break;
                default:
                    Log.e(TAG, "Greetings from the future!!!");
                    Log.i(TAG, "ServiceHandler handleMessage: no case for message " + msg.what);
                    break;
            }

            while (mData == null) {
                synchronized (this) {
                    try {
                        wait(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            stopSelf(msg.arg1);
        }
    }

    /**
     * register with the LocationManager for a single GPS event
     */
    private void requestSingleGpsUpdate() {
        try {
            mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, mServiceLooper);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void requestForecastObject(Location location) {
        String link = buildForecastUrl(location.getLatitude(), location.getLongitude());
        HttpGet request = new HttpGet(link);
        AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        HttpResponse response;

        try {
            response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                response.getEntity().writeTo(output);
                output.close();

                String result = output.toString();

                mData = new JSONObject(result);
                Log.i(TAG, mData.toString());
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        client.close();
    }

    private String buildForecastUrl(Double latitude, Double longitude) {
        return API_URL + API_KEY + "/" + latitude.toString() + "," + longitude.toString();
    }

    private String buildForecastUrlWithDate(Double latitude, Double longitude) {
        Locale[] locales = DateFormat.getAvailableLocales();
        Locale locale = (locales.length > 0) ? locales[0] : Locale.US;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, locale);
        sdf.setTimeZone(TimeZone.getTimeZone(locale.toString()));
        return API_URL + API_KEY + "/" + latitude.toString() + "," + longitude.toString() + "," + sdf.format(new Date());
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Messenger mMessenger;
    private LocationManager mLocationManager;
    private JSONObject mData;

    private static final String TAG = "RequestDataService";
    private final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final String API_URL = "https://api.forecast.io/forecast/";
    private final String API_KEY = "5e07f9dc4b8932b18f19cea015e5512c";

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Thread.MIN_PRIORITY);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mMessenger = new Messenger(mServiceHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Message msg = mServiceHandler.obtainMessage();
        mServiceHandler.sendMessage(msg);
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {

    }
}