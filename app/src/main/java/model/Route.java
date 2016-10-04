package model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by lequan on 10/2/2016.
 */
public class Route
{
    ArrayList<LatLng> route;

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public void setDistance(String distance)
    {
        this.distance = distance;
    }

    String duration;
    String distance;

    public ArrayList<LatLng> getRoute()
    {
        return route;
    }

    public String getDuration()
    {
        return duration;
    }

    public String getDistance()
    {
        return distance;
    }

    public Route(ArrayList<LatLng> route, String duration, String distance)
    {
        this.route = route;
        this.duration = duration;
        this.distance = distance;
    }

    public Route()
    {
        route = new ArrayList<>();
    }

    public void add(LatLng point)
    {
        route.add(point);
    }

    public void addAll(ArrayList<LatLng> points)
    {
        route.addAll(points);
    }

    public int lenght()
    {
        return route.size();
    }

    public LatLng get(int index)
    {
        return route.get(index);
    }
}
