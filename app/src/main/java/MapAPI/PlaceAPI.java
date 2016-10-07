package mapAPI;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import model.Place;
import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 5/13/2016.
 */
public class PlaceAPI
{
    public static String createPlaceUrlRequest(String apiKey, double latitude, double longitude, String type, int radius)
    {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        url.append("location=" + latitude + "," + longitude);
        url.append("&radius=" + radius * 1000);     // kilometer
        url.append("&types=" + type);
        url.append("&key=" + apiKey);
        //Log.d("123", "url = " + googlePlacesUrl.toString());
        return url.toString();
    }

    public static ArrayList<Place> getPlaces(JSONObject object)
    {
        if (object == null)
            return null;

        ArrayList<Place> placesList = new ArrayList<>();
        try
        {
            JSONArray places = object.getJSONArray("results");
            for (int i = 0; i < places.length(); ++i)
            {
                JSONObject item = places.getJSONObject(i);
                JSONObject location = item.getJSONObject("geometry").getJSONObject("location");
                Place place = new Place(location.getDouble("lat"), location.getDouble("lng"), item.getString("name"), item.getString("vicinity"));
                placesList.add(place);
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return placesList;
    }
}