package model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lequan on 10/8/2016.
 */
public class Path implements Serializable
{
    ArrayList<LatLng> path;

    public LatLng getStart()
    {
        if (path.size() > 0)
            return path.get(0);
        return null;
    }

    public LatLng getEnd()
    {
        if (path.size() > 0)
            return path.get(path.size()-1);
        return null;
    }

    public void addAll(ArrayList<LatLng> points)
    {
     path.addAll(points)   ;
    }

    public void add(LatLng point)
    {
     path.add(point);
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public void setDistance(int distance)
    {
        this.distance = distance;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public ArrayList<LatLng> getPath()
    {
        return path;
    }

    public Path()
    {
        path = new ArrayList<>();
    }


    int duration;       // seconds
    int distance;       // meters
}
