package AsyncTask;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import Listener.OnLoadListener;
import MapAPI.DirectionAPI;
import Utils.JsonUtils;

/**
 * Created by lequan on 4/22/2016.
 */
public class DirectionAst extends AsyncTask<LatLng, Integer, ArrayList<LatLng>>
{
    OnLoadListener<ArrayList<LatLng>> listener;
    String mode;

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public void setOnLoadListener(OnLoadListener listener)
    {
        this.listener = listener;
    }

    public DirectionAst()
    {
        mode = "driving";
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> result)
    {
        listener.onLoaded(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected ArrayList<LatLng> doInBackground(LatLng... params)
    {
        String url = DirectionAPI.createDirectionUrlRequest(params[0], params[1], mode);
        return DirectionAPI.getDirection(JsonUtils.getJSON(url));
    }

}
