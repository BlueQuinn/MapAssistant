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
public class TrafficUtils
{
    double a, b;
    LatLng start, end;

    public TrafficUtils(LatLng[] point, double big)
    {
        start = point[0];
        end = point[1];
    }

    public boolean isNear(double lat, double lng)
    {
        if (1.0 * (lat * lat / a / a + lng * lng / b / b) <= 1)
        {
            return true;
        }
        return false;
    }

    public LatLng intersect(LatLng A, LatLng B)
    {
        return null;
    }

}
