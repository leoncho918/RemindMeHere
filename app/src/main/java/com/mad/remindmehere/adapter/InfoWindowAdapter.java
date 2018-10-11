package com.mad.remindmehere.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mad.remindmehere.R;

/**
 * An adapter that provides a view for customised rendering of info windows
 */
public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    //Activity variable to store the activity that creates an adapter object
    private Activity context;

    /**
     * Constructor that takes the calling activity's reference as a parameter
     */
    public InfoWindowAdapter(Activity context) {
        //Save activity
        this.context = context;
    }

    /**
     * Provides a custom info window for a marker
     * @param marker
     * @return
     */
    @Override
    public View getInfoWindow(Marker marker) {
        //Return null
        return null;
    }

    /**
     * Provides custom contents for the default info window frame of a marker
     * @param marker
     * @return
     */
    @Override
    public View getInfoContents(Marker marker) {
        //Create new view object to save custom layout view
        View view = context.getLayoutInflater().inflate(R.layout.infowindow, null);

        //Store TextView ui widgets from custom layout view
        TextView nameTv = (TextView) view.findViewById(R.id.name_Tv);
        TextView addressTv = (TextView) view.findViewById(R.id.desc_Tv);

        //Set textview text to marker title and snippet
        nameTv.setText(marker.getTitle());
        addressTv.setText(marker.getSnippet());

        //Return custom layout view
        return view;
    }
}
