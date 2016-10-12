package utils;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import model.TrafficLine;
import model.TrafficOption;

/**
 * Created by lequan on 10/4/2016.
 */
public class TrafficLineUtils
{
    BitmapDescriptor icon;
    String level;
    int meta;
    int mediumColor, highColor;

    public TrafficLineUtils(int meta, int mediumColor, int highColor)
    {
        this.meta = meta;
        this.mediumColor = mediumColor;
        this.highColor = highColor;
    }

    public TrafficOption getOption(TrafficLine trafficLine)
    {
        MarkerOptions markerOptions;
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(trafficLine.getStart()).add(trafficLine.getEnd()).width(15);
        if (trafficLine.getRating() < 2 * meta)
        {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_medium);
            level = "Ùn tắc giao thông";
            polylineOptions.color(mediumColor);
        }
        else
        {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_high);
            level = "Kẹt xe";
            polylineOptions.color(highColor);
        }
        markerOptions = new MarkerOptions().icon(icon);
        markerOptions.position(trafficLine.getCenter())
               .title(level).snippet(Integer.toString(trafficLine.getRating()) + " người đã thông báo");

        return new TrafficOption(markerOptions, polylineOptions);
    }

}
