package com.udev.ordinaryweather;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Vic on 13/9/12.
 */
public class RequestDataService extends Service implements LocationListener {

    public class BootBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent startServiceIntent = new Intent(context, RequestDataService.class);
            context.startService(startServiceIntent);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        requestForecastObject(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private LocationManager mLocationManager;
    private JSONObject mData;

    private static final String TAG = "RequestDataService";
    private final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final String API_URL = "https://api.forecast.io/forecast/";
    private final String API_KEY = "5e07f9dc4b8932b18f19cea015e5512c";

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            //todo:wait for forecast data to load
            while (mData == null) {
                synchronized (this) {
                    try {
                        wait(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Intent intent = new Intent("DISPLAY_FORECAST");
            intent.putExtra("data", mData.toString());
            startActivity(intent);

            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Thread.MIN_PRIORITY);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        try {
            mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, mServiceLooper);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

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
}
