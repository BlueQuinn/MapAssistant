package utils;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import model.TrafficCircle;

/**
 * Created by lequan on 10/4/2016.
 */
public class TrafficCircleUtils
{
    BitmapDescriptor icon;
    String level;
    int meta;

    public TrafficCircleUtils(int meta)
    {
        this.meta = meta;
    }

    public MarkerOptions getOption(TrafficCircle trafficCircle)
    {
        MarkerOptions markerOptions;
        if (trafficCircle.getRating() < 2 * meta)
        {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_medium);
            level = "Ùn tắc giao thông";
        }
        else
        {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_high);
            level = "Kẹt xe";
        }
        markerOptions = new MarkerOptions().icon(icon);
        markerOptions.position(trafficCircle.getCenter())
               .title(level).snippet(Integer.toString(trafficCircle.getRating()) + " người đã thông báo");

        return markerOptions;
    }

}
