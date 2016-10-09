package asyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import listener.OnLoadListener;
import mapAPI.DirectionAPI;
import model.Route;
import utils.JsonUtils;

/**
 * Created by lequan on 4/22/2016.
 */
public class DirectionAst extends AsyncTask<LatLng, Integer, Route>
{
    OnLoadListener<Route> listener;
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
    protected void onPostExecute(Route result)
    {
        listener.onFinish(result);
        super.onPostExecute(result);
    }

    @Override
    protected Route doInBackground(LatLng... params)
    {
        String url;
        if (params.length > 2)
        {
            url = DirectionAPI.createDirectionUrlRequest(params);
        }
        else
        {
            url = DirectionAPI.createDirectionUrlRequest(params[0], params[1], mode);
        }
        Log.d("123", url);
        Log.d("123", " s " +params.length);
        return DirectionAPI.getDirection(JsonUtils.getJSON(url));
    }

}
