package model;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by lequan on 10/7/2016.
 */
public class TrafficOption
{
    MarkerOptions markerOptions;
    PolylineOptions polylineOptions;

    public MarkerOptions getMarkerOptions()
    {
        return markerOptions;
    }

    public PolylineOptions getPolylineOptions()
    {
        return polylineOptions;
    }

    public TrafficOption(MarkerOptions markerOptions, PolylineOptions polylineOptions)
    {

        this.markerOptions = markerOptions;
        this.polylineOptions = polylineOptions;
    }
}
