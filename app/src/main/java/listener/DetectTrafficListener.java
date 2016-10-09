package listener;

import com.google.android.gms.maps.model.LatLng;

import model.Route;

/**
 * Created by lequan on 10/9/2016.
 */
public interface DetectTrafficListener
{
    void onFinish(Route route, int i, LatLng[] traffic);
}
