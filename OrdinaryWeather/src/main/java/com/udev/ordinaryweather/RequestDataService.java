package com.udev.ordinaryweather;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

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

    private final class ClientMessageHandler extends Handler {
        public ClientMessageHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case RequestDataService.REQUEST_DATA:
                    requestSingleGpsUpdate();
                    break;
                default:
                    Log.i(TAG, "ClientMessageHandler handleMessage: no case for message " + msg.what + " with Message.arg1 == " + msg.arg1);
                    break;
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
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, mLooper);
            Log.i(TAG, "requestSingleGpsUpdate");
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

                Bundle data = new Bundle();
                data.putString("data", mData.toString());

                //todo:broadcast message
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

    private Looper mLooper;
    private ClientMessageHandler mClientMessageHandler;
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

        mLooper = thread.getLooper();
        mClientMessageHandler = new ClientMessageHandler(mLooper);
        mMessenger = new Messenger(mClientMessageHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {

    }
}