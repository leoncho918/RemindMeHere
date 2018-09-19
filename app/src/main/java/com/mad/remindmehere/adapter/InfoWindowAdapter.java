package com.mad.remindmehere.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mad.remindmehere.R;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public InfoWindowAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.infowindow, null);

        TextView nameTv = (TextView) view.findViewById(R.id.name_Tv);
        TextView addressTv = (TextView) view.findViewById(R.id.desc_Tv);

        nameTv.setText(marker.getTitle());
        addressTv.setText(marker.getSnippet());

        return view;
    }
}
