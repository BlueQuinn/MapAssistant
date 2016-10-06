package mapAPI;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import model.Route;
import utils.PolyUtils;

/**
 * Created by lequan on 4/22/2016.
 */
public class DirectionAPI
{
    public static String createDirectionUrlRequest(LatLng start, LatLng end, String mode)
    {
        String urlString = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=" + mode;
        return urlString;
    }

    public static String createDirectionUrlRequest(LatLng... waypoint)
    {
        String urlString = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + waypoint[0].latitude + "," + waypoint[0].longitude
                + "&destination=" + waypoint[1].latitude + "," + waypoint[1].longitude
                + "&sensor=false&units=metric&mode="
                + "&waypoint=";
        for (int i = 2; i < waypoint.length; ++i)
        {
            urlString += waypoint[i].latitude + "," + waypoint[i].longitude + "&";
        }
        urlString = urlString.substring(0, urlString.length() - 1);
        return urlString;
    }

    public static Route getDirection(JSONObject object)
    {
        if (object == null)
        {
            return null;
        }

        Route route = new Route();
        try
        {
            JSONArray direction = object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            route.setDistance(0);
            route.setDuration(0);
            Route[] path = new Route[direction.length()];
            for (int i = 0; i < direction.length(); ++i)
            {
                path[i] = getPath(direction.getJSONObject(i));
                route.addAll(path[i].getRoute());
                route.setDistance(route.getDistance() + path[i].getDistance());
                route.setDuration(route.getDuration() + path[i].getDuration());
                route.addPath(path[i].getPath());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return route;
    }

    static Route getPath(JSONObject path) throws JSONException
    {
        Route route = new Route();

        // distance
        JSONObject distance = path.getJSONObject("distance");
        route.setDistance(distance.getInt("value"));

        // duration
        JSONObject duration = path.getJSONObject("duration");
        route.setDuration(duration.getInt("value"));

        // start
        JSONObject start = path.getJSONObject("start_location");
        //double lat = start.getDouble("lat");
        //double lng = start.getDouble("lng");
        LatLng point = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
        route.add(point);
        route.setStart(point);

        // steps
        JSONArray steps = path.getJSONArray("steps");
        for (int i = 0; i < steps.length(); ++i)
        {
            String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
            ArrayList<LatLng> points = PolyUtils.decode(polyline);
            route.addAll(points);
        }

        // end
        JSONObject end = path.getJSONObject("end_location");
        //lat = end.getDouble("lat");
        //lng = end.getDouble("lng");
        point = new LatLng(end.getDouble("lat"), end.getDouble("lng"));
        route.add(point);
        route.setEnd(point);

        return route;
    }
}
