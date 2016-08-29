package MapAPI;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

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

    public static ArrayList<LatLng> getDirection(JSONObject object)
    {
        if (object == null)
            return null;

        ArrayList<LatLng> directionPoints = new ArrayList<>();
        try
        {
            JSONObject direction = object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);

            // start
            JSONObject start = direction.getJSONObject("start_location");
            double lat = start.getDouble("lat");
            double lng = start.getDouble("lng");
            directionPoints.add(new LatLng(lat, lng));

            // steps
            JSONArray steps = direction.getJSONArray("steps");
            for (int i = 0; i < steps.length(); ++i)
            {
                String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                ArrayList<LatLng> points = decodePoly(polyline);
                directionPoints.addAll(points);
            }

            // end
            JSONObject end = direction.getJSONObject("end_location");
            lat = end.getDouble("lat");
            lng = end.getDouble("lng");
            directionPoints.add(new LatLng(lat, lng));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return directionPoints;
    }

    static ArrayList<LatLng> decodePoly(String encoded)
    {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len)
        {
            int b, shift = 0, result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}
