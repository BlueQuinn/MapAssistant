package utils;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import model.Traffic;

/**
 * Created by lequan on 10/4/2016.
 */
public class MarkerUtils
{
    BitmapDescriptor icon;
    String level;
    int meta;

    public MarkerUtils(int meta)
    {
        this.meta = meta;
    }

    public MarkerOptions getOption(Traffic traffic)
    {
        MarkerOptions options;
        if (traffic.getVote() < 2 * meta)
        {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_medium);
            level = "Ùn tắc giao thông";
        }
        else
        {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_high);
            level = "Kẹt xe";
        }
        options = new MarkerOptions().icon(icon);
        options.position(new LatLng(traffic.getLat(), traffic.getLng()))
               .title(level).snippet(Integer.toString(traffic.getVote()) + " người đã thông báo");
        return options;
    }

}
