package mapAPI;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lequan on 10/8/2016.
 */
public class RoadAPI
{
    public static String createRequest(String apiKey, LatLng location)
    {
        String url = "https://roads.googleapis.com/v1/nearestRoads?points="
                + location.latitude + "," + location.longitude
                + "&key=" + apiKey;
        return url;
    }

    public static ArrayList<LatLng> getRoad(JSONObject data)
    {
        ArrayList<LatLng> road = new ArrayList<>();
        try
        {
            JSONArray snappedPoints = data.getJSONArray("snappedPoints");
            for (int i=0;i<snappedPoints.length();++i)
            {
                JSONObject location = snappedPoints.getJSONObject(i).getJSONObject("location");
                double lat = location.getDouble("latitude");
                double lng = location.getDouble("longitude");
                road.add(new LatLng(lat, lng));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return road;
    }

}
