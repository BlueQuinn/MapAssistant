package asyncTask;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import listener.OnLoadListener;
import mapAPI.RoadAPI;
import model.Route;
import utils.JsonUtils;

/**
 * Created by lequan on 10/8/2016.
 */
public class RoadAst extends AsyncTask<LatLng, Void,  ArrayList<LatLng>>
{
    String apiKey;

    public RoadAst(String apiKey)
    {
        this.apiKey = apiKey;
    }

    @Override
    protected  ArrayList<LatLng> doInBackground(LatLng... params)
    {
        String url = RoadAPI.createRequest(apiKey, params[0]);
        return RoadAPI.getRoad(JsonUtils.getJSON(url));
    }

    OnLoadListener< ArrayList<LatLng>> listener;

    public void setOnLoadListener(OnLoadListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> road)
    {
        listener.onFinish(road);
        super.onPostExecute(road);
    }
}
