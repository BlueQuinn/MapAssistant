package utils;

import com.firebase.client.DataSnapshot;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import model.Route;
import model.Shortcut;
import model.TrafficCircle;
import model.TrafficLine;

/**
 * Created by lequan on 10/11/2016.
 */
public class FirebaseUtils
{
    public static ArrayList<TrafficCircle> getTrafficCircle(DataSnapshot dataSnapshot, int meta)
    {
        ArrayList<TrafficCircle> trafficCircles = new ArrayList<>();
        DataSnapshot circle = dataSnapshot.child("circle");
        for (DataSnapshot data : circle.getChildren())
        {
            int rate = ((Long) data.child("rate").getValue()).intValue();
            if (rate > meta)
            {
                int id = ((Long) data.child("id").getValue()).intValue();
                double lat = (double) data.child("lat").getValue();
                double lng = (double) data.child("lng").getValue();
                int radius = ((Long) data.child("radius").getValue()).intValue();

                DataSnapshot shortcutData  = data.child("shortcut");
                ArrayList<Shortcut> shortcuts = new ArrayList<>();
                for (DataSnapshot i : shortcutData.getChildren())
                {
                    int distance = ((Long) data.child("distance").getValue()).intValue();
                    int duration = ((Long) data.child("duration").getValue()).intValue();
                    String route = (String) data.child("route").getValue();
                    int like = ((Long) data.child("like").getValue()).intValue();
                    shortcuts.add(new Shortcut(route, like, duration, distance));
                }
                trafficCircles.add(new TrafficCircle(id, new LatLng(lat, lng), radius, rate, shortcuts));
            }
        }
        return trafficCircles;
    }

    public static ArrayList<TrafficLine> getTrafficLine(DataSnapshot dataSnapshot, int meta)
    {
        ArrayList<TrafficLine> trafficLine = new ArrayList<>();
        DataSnapshot line = dataSnapshot.child("line");
        for (DataSnapshot data : line.getChildren())
        {
            int rate = ((Long) data.child("rate").getValue()).intValue();
            if (rate > meta)
            {
                int id = ((Long) data.child("id").getValue()).intValue();
                double lat1 = (double) data.child("lat1").getValue();
                double lng1 = (double) data.child("lng1").getValue();
                double lat2 = (double) data.child("lat2").getValue();
                double lng2 = (double) data.child("lng2").getValue();

                DataSnapshot shortcutData  = data.child("shortcut");
                ArrayList<Shortcut> shortcuts = new ArrayList<>();
                for (DataSnapshot i : shortcutData.getChildren())
                {
                    int distance = ((Long) data.child("distance").getValue()).intValue();
                    int duration = ((Long) data.child("duration").getValue()).intValue();
                    String route = (String) data.child("route").getValue();
                    int like = ((Long) data.child("like").getValue()).intValue();
                    shortcuts.add(new Shortcut(route, like, duration, distance));
                }
                trafficLine.add(new TrafficLine(id, lat1, lng1, lat2, lng2, rate, shortcuts));
            }
        }
        return trafficLine;
    }
}
