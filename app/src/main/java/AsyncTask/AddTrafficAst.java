package asyncTask;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

import model.TrafficLine;
import listener.OnLoadListener;
import model.TrafficOption;
import utils.TrafficOptionUtils;

/**
 * Created by lequan on 8/29/2016.
 */
public class AddTrafficAst extends AsyncTask<Integer, TrafficOption, Boolean>
{
    ArrayList<TrafficLine> listTrafficLine;
    OnLoadListener<Boolean> listener;
    GoogleMap map;

    public AddTrafficAst(ArrayList<TrafficLine> listTrafficLine, GoogleMap map)
    {
        this.listTrafficLine = listTrafficLine;
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
        int mediumColor = params[1];
        int highColor = params[2];
        TrafficOptionUtils utils = new TrafficOptionUtils(meta, mediumColor, highColor);
        for (TrafficLine trafficLine : listTrafficLine)
        {
            publishProgress(utils.getOption(trafficLine));
        }
        return true;
    }
}
