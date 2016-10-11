package model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lequan on 10/11/2016.
 */
public class TrafficCircle
{
    LatLng center;

    public LatLng getCenter()
    {
        return center;
    }

    public TrafficCircle(LatLng center, int radius, int rate)
    {

        this.center = center;
        this.radius = radius;
        this.rate = rate;
    }

    int radius, rate;


    public int getRadius()
    {
        return radius;
    }

    public int getRate()
    {
        return rate;
    }

}
