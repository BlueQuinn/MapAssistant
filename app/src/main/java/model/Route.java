package model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by lequan on 10/2/2016.
 */
public class Route
{
    ArrayList<LatLng> route;
    LatLng start, end;

    ArrayList<LatLng[]> path;

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public void setDistance(int distance)
    {
        this.distance = distance;
    }

    int duration;       // meters
    int distance;       // seconds

    public ArrayList<LatLng> getRoute()
    {
        return route;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public String getInformation()
    {
        String time;
        int hour = duration / 3600;
        int minute = (duration - hour * 3600) / 60;

        if (hour == 0)
        {
            time = Integer.toString(minute) + "p";
        }
        else
        {
            time = Integer.toString(hour) + "h";
            if (minute != 0)
            {
                time += Integer.toString(minute) + "p";
            }
        }

        String dtc;
        if (distance < 1000)
        {
            dtc = Integer.toString(distance) + "m";
        }
        else
        {
            dtc = Float.toString(Math.round(distance * 1000) / 1000) + "km";
        }

        return dtc + " - " + time;
    }

    public Route()
    {
        route = new ArrayList<>();
        path = new ArrayList<>();
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

    public void setStart(LatLng start)
    {
        this.start = start;
    }

    public void setEnd(LatLng end)
    {
        this.end = end;
    }

    public LatLng[] getPath(int i)
    {
        return path.get(i);
    }

    public LatLng[] getPath()
    {
        return new LatLng[]{start, end};
    }

    public int pathCount()
    {
        return path.size();
    }

    public void addPath(LatLng[] point)
    {
        path.add(point);
    }
}
