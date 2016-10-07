package asyncTask;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

import model.Traffic;
import listener.OnLoadListener;
import model.TrafficOption;
import utils.TrafficOptionUtils;

/**
 * Created by lequan on 8/29/2016.
 */
public class AddTrafficAst extends AsyncTask<Integer, TrafficOption, Boolean>
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
    protected void onProgressUpdate(TrafficOption... values)
    {
        super.onProgressUpdate(values);

        if (values.length > 0)
        {
            TrafficOption option = values[0];
            map.addMarker(option.getMarkerOptions());
            map.addPolyline(option.getPolylineOptions());
        }
    }

    @Override
    protected Boolean doInBackground(Integer... params)
    {
        int meta = params[0];
        TrafficOptionUtils utils = new TrafficOptionUtils(meta);
        for (Traffic traffic : listTraffic)
        {
            publishProgress(utils.getOption(traffic));
        }
        return true;
    }
}
