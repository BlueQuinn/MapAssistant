package utils;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

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
        for (DataSnapshot data : dataSnapshot.getChildren())
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
                    int shortcutId = ((Long) i.child("id").getValue()).intValue();
                    String route = (String) i.child("route").getValue();
                    int like = ((Long) i.child("like").getValue()).intValue();
                    int distance = ((Long) i.child("distance").getValue()).intValue();
                    int duration = ((Long) i.child("duration").getValue()).intValue();
                    shortcuts.add(new Shortcut(shortcutId, route, like, duration, distance));
                }
                trafficCircles.add(new TrafficCircle(id, new LatLng(lat, lng), radius, rate, shortcuts));
            }
        }
        return trafficCircles;
    }

    public static ArrayList<TrafficLine> getTrafficLine(DataSnapshot dataSnapshot, int meta)
    {
        ArrayList<TrafficLine> trafficLine = new ArrayList<>();
        for (DataSnapshot data : dataSnapshot.getChildren())
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
                    int shortcutId = ((Long) i.child("id").getValue()).intValue();
                    int distance = ((Long) i.child("distance").getValue()).intValue();
                    int duration = ((Long) i.child("duration").getValue()).intValue();
                    String route = (String) i.child("route").getValue();
                    int like = ((Long) i.child("like").getValue()).intValue();
                    shortcuts.add(new Shortcut(shortcutId, route, like, duration, distance));
                }
                trafficLine.add(new TrafficLine(id, lat1, lng1, lat2, lng2, rate, shortcuts));
            }
        }
        return trafficLine;
    }

    public static Firebase getShortcutRef(DataSnapshot snapshot)
    {
        if (snapshot.getValue() instanceof HashMap)
        {
            HashMap<String, Object> wtf = (HashMap<String, Object>) snapshot.getValue();
            ArrayList<String> keySet = new ArrayList<>(wtf.keySet());
            if (keySet.size() > 0)
            {
                String key = keySet.get(0);     // be careful
                return snapshot.child(key).child("shortcut").getRef();
            }
        }
        return null;
    }

    public static Firebase getShortcutNode(DataSnapshot snapshot)
    {
        if (snapshot.getValue() instanceof HashMap)
        {
            HashMap<String, Object> wtf = (HashMap<String, Object>) snapshot.getValue();
            ArrayList<String> keySet = new ArrayList<>(wtf.keySet());
            if (keySet.size() > 0)
            {
                String key = keySet.get(0);     // be careful
                return snapshot.child(key).getRef();
            }
        }
        return null;
    }
}
