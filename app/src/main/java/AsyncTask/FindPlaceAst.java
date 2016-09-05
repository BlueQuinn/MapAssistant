package AsyncTask;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import model.Place;
import Listener.OnLoadListener;
import MapAPI.PlaceAPI;
import Utils.JsonUtils;

/**
 * Created by lequan on 5/13/2016.
 */
public class FindPlaceAst extends AsyncTask<Double, Integer, ArrayList<Place>>
{
    Context context;
    OnLoadListener<ArrayList<Place>> listener;
    String type;
    int radius;

    public void setOnLoadListener(OnLoadListener listener)
    {
        this.listener = listener;
    }

    public FindPlaceAst(Context context, String type, int radius)
    {
        this.context = context;
        this.type = type;
        this.radius = radius;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Place> result)
    {
        listener.onFinish(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected ArrayList<Place> doInBackground(Double... params)
    {
        String url = PlaceAPI.createPlaceUrlRequest(context, params[0].doubleValue(), params[1].doubleValue(), type, radius);
        return PlaceAPI.getPlaces(JsonUtils.getJSON(url));
    }

}
