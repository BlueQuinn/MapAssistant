package utils;

import com.firebase.client.DataSnapshot;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import model.TrafficCircle;
import model.TrafficLine;

/**
 * Created by lequan on 10/11/2016.
 */
public class FirebaseUtils
{
    public static ArrayList<TrafficCircle> getTrafficCircle(DataSnapshot data)
    {
        ArrayList<TrafficCircle> trafficCircles = new ArrayList<>();
        DataSnapshot circle = data.child("circle");
        for (DataSnapshot c : circle.getChildren())
        {
            double lat = (double) c.child("lat").getValue();
            double lng = (double) c.child("lng").getValue();
            int radius = ((Long) c.child("radius").getValue()).intValue();
            int rate = ((Long) c.child("rate").getValue()).intValue();
            trafficCircles.add(new TrafficCircle(new LatLng(lat, lng), radius, rate));
        }
        return trafficCircles;
    }

    public static ArrayList<TrafficLine> getTrafficLine(DataSnapshot data)
    {
        ArrayList<TrafficLine> trafficLine = new ArrayList<>();
        DataSnapshot line = data.child("line");
        for (DataSnapshot l : line.getChildren())
        {
            double lat1 = (double) l.child("lat1").getValue();
            double lng1 = (double) l.child("lng1").getValue();
            double lat2 = (double) l.child("lat2").getValue();
            double lng2 = (double) l.child("lng2").getValue();
            int rate = ((Long) l.child("rate").getValue()).intValue();
            trafficLine.add(new TrafficLine(lat1, lng1, lat2, lng2, rate));
        }
        return trafficLine;
    }
}
