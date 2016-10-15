package utils;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import model.Path;
import model.Route;
import model.TrafficCircle;
import model.TrafficLine;

import static utils.TimeUtils.toMinutes;

/**
 * Created by lequan on 10/4/2016.
 */
public class TrafficUtils
{
    public static String getTimeNode()
    {
        String date = new SimpleDateFormat("HH:mm").format(new Date());
        final int timeNow = toMinutes(date);
        int t = timeNow / 30, tDown = t * 30, tUp = (t + 1) * 30;
        if (timeNow - tDown < tUp - timeNow)
        {
            return Integer.toString(tDown);
        }
        else
        {
            return Integer.toString(tUp);
        }
    }

    public static ArrayList<TrafficCircle> getCircleJam(ArrayList<TrafficCircle> trafficCircles, Route route)
    {
        ArrayList<TrafficCircle> traffic = new ArrayList<>();
        for (int i = 0; i < route.pathCount(); ++i)
        {
            Path path = route.getPath(i);
            for (TrafficCircle circle : trafficCircles)
            {
                if (PolyUtils.isLocationOnPath(circle.getCenter(), path.getPath(), false, circle.getRadius()))
                {
                    traffic.add(circle);
                }
            }
        }
        return traffic;
    }

    public static ArrayList<TrafficLine> getLineJam(ArrayList<TrafficLine> trafficLines, Route route)
    {
        ArrayList<TrafficLine> traffic = new ArrayList<>();
        for (int i = 0; i < route.pathCount(); ++i)
        {
            Path path = route.getPath(i);
            for (TrafficLine line : trafficLines)
            {
                if (PolyUtils.isLocationOnPath(line.getCenter(), path.getPath(), false, line.length()/2))
                {
                    traffic.add(line);
                }
            }
        }
        return traffic;
    }















    public static ArrayList<LatLng> getNearbyCircleJam(ArrayList<TrafficCircle> trafficCircles, LatLng pos, int radius)
    {
        ArrayList<LatLng> traffic = new ArrayList<>();
            for (TrafficCircle circle : trafficCircles)
            {
                if (MapUtils.inCircle(circle.getCenter(), pos, radius))
                {
                    traffic.add(circle.getCenter());
                }
            }
        return traffic;
    }

    public static ArrayList<LatLng> getNearbyLineJam(ArrayList<TrafficLine> trafficLines, LatLng pos, int radius)
    {
        ArrayList<LatLng> traffic = new ArrayList<>();
        for (TrafficLine line : trafficLines)
        {
            if (MapUtils.inCircle(line.getStart(), pos, radius) && MapUtils.inCircle(line.getEnd(), pos, radius))
            {
                traffic.add(line.getCenter());
            }
        }
        return traffic;
    }



















    public static ArrayList<LatLng> getNearbyCircleJam(ArrayList<TrafficCircle> trafficCircles, ArrayList<LatLng> route)
    {
        ArrayList<LatLng> traffic = new ArrayList<>();
        for (TrafficCircle circle : trafficCircles)
        {
            if (PolyUtils.isLocationOnPath(circle.getCenter(), route, false, 100))
            {
                traffic.add(circle.getCenter());
            }
        }
        return traffic;
    }

    public static ArrayList<LatLng> getNearbyLineJam(ArrayList<TrafficLine> trafficLines, LatLng center)
    {
        ArrayList<LatLng> traffic = new ArrayList<>();
            for (TrafficLine line : trafficLines)
            {
                if (PolyUtils.isLocationOnPath(center, line.getPolyline(), false, 100))
                {
                    traffic.add(line.getCenter());
                }
        }
        return traffic;
    }



    public static ArrayList<LatLng> getNearbyJam(ArrayList<TrafficLine> trafficLines, ArrayList<TrafficCircle> trafficCircles, LatLng pos, int radius)
    {
        ArrayList<LatLng> jam = new ArrayList<>();
        for (LatLng i : getNearbyCircleJam(trafficCircles, pos, radius))
            jam.add(i);
        for (LatLng i : getNearbyLineJam(trafficLines, pos, radius))
            jam.add(i);
        for (LatLng i : getNearbyCircleJam(trafficCircles, pos, radius))
            jam.add(i);
        for (LatLng i : getNearbyLineJam(trafficLines, pos))
            jam.add(i);
        return jam;
    }
}
