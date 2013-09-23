package com.udev.ordinaryweather;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Vic on 13/9/23.
 */
public class DisplayWeatherFragment extends Fragment {
    public DisplayWeatherFragment() {
        super();
    }

    @Override
    public void onStart() {
        super.onStart();
        //todo:get weather data
//        updateDisplay(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_display_weather, container, false);
    }
}