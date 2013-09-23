package com.udev.ordinaryweather;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Vic on 13/9/23.
 */
public class LoadingFragment extends ListFragment{
    public LoadingFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] items = new String[]{
                "currently",
                "minutely",
                "hourly",
                "daily"
        };

        setListAdapter(new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                items));
    }
}
