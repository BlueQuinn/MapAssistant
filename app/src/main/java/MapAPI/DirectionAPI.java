package mapAPI;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import model.Path;
import model.Route;
import utils.PolyUtils;

/**
 * Created by lequan on 4/22/2016.
 */
public class DirectionAPI
{
    public static String createDirectionUrlRequest(LatLng start, LatLng end, String mode)
    {
        String url = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=" + mode;
        return url;
    }

    public static String createDirectionUrlRequest(LatLng... waypoint)      // two first points will be origin and destination
    {
        String url = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + waypoint[0].latitude + "," + waypoint[0].longitude
                + "&destination=" + waypoint[1].latitude + "," + waypoint[1].longitude
                + "&sensor=false&units=metric&mode=driving"
                + "&waypoints=";
        for (int i = 2; i < waypoint.length; ++i)
        {
            url += waypoint[i].latitude + "," + waypoint[i].longitude + "|";
        }
        url = url.substring(0, url.length() - 1);
        return url;
    }

    public static Route getDirection(JSONObject object)
    {
        if (object == null)
        {
            return null;
        }

        Route route = null;
        try
        {
            JSONArray direction = object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            ArrayList<Path> path = new ArrayList<>();
            for (int i = 0; i < direction.length(); ++i)
            {
                path.add(getPath(direction.getJSONObject(i)));
            }
            if (path.size() > 0)
            {
                route = new Route(path);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return route;
    }

    static Path getPath(JSONObject path) throws JSONException
    {
        Path route = new Path();

        // distance
        JSONObject distance = path.getJSONObject("distance");
        route.setDistance(distance.getInt("value"));

        // duration
        JSONObject duration = path.getJSONObject("duration");
        route.setDuration(duration.getInt("value"));

        // start
        JSONObject start = path.getJSONObject("start_location");
        LatLng point = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
        route.add(point);

        // steps
        JSONArray steps = path.getJSONArray("steps");
        for (int i = 0; i < steps.length(); ++i)
        {
            String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

            //long startTime = System.currentTimeMillis();
            ArrayList<LatLng> points = PolyUtils.decode(polyline);
            //long elapsedTime = System.currentTimeMillis() - startTime;
            //Log.d("time", "time = " + elapsedTime);
            //Log.d("time", "size = " + points.size());

            route.addAll(points);
        }

        // end
        JSONObject end = path.getJSONObject("end_location");
        point = new LatLng(end.getDouble("lat"), end.getDouble("lng"));
        route.add(point);

        return route;
    }
}
