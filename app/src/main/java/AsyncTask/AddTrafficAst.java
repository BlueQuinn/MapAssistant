package asyncTask;

import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import model.Traffic;
import listener.OnLoadListener;
import utils.MarkerUtils;
import utils.TrafficUtils;

/**
 * Created by lequan on 8/29/2016.
 */
public class AddTrafficAst extends AsyncTask<Integer, MarkerOptions, Boolean>
{
    ArrayList<Traffic> listTraffic;
    OnLoadListener<Boolean> listener;
    GoogleMap map;

    public AddTrafficAst(ArrayList<Traffic> listTraffic, GoogleMap map)
    {
        this.listTraffic = listTraffic;
        this.map = map;
    }

    public void setListener(OnLoadListener<Boolean> listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        listener.onFinish(true);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(MarkerOptions... values)
    {
        super.onProgressUpdate(values);

        if (values.length > 0)
        {
            map.addMarker(values[0]);
        }
    }

    @Override
    protected Boolean doInBackground(Integer... params)
    {
        int meta = params[0];
        MarkerUtils marker = new MarkerUtils(meta);
        for (Traffic traffic : listTraffic)
        {
            publishProgress(marker.getOption(traffic));
        }
        return true;
    }
}
