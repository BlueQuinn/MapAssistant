package asyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import model.TrafficCircle;
import model.TrafficLine;
import listener.OnLoadListener;
import model.TrafficOption;
import model.TrafficType;
import utils.TrafficCircleUtils;
import utils.TrafficLineUtils;

/**
 * Created by lequan on 8/29/2016.
 */
public class AddTrafficAst extends AsyncTask<Integer, Object, HashMap<String, TrafficType>>
{
    ArrayList<TrafficLine> listLine;
    ArrayList<TrafficCircle> listCircle;
    OnLoadListener<HashMap<String, TrafficType>> listener;
    GoogleMap map;
    boolean type = true;
    HashMap<String, TrafficType> hashMapType;

    public AddTrafficAst(ArrayList<TrafficLine> listLine, ArrayList<TrafficCircle> listCircle, GoogleMap map)
    {
        this.listLine = listLine;
        this.listCircle = listCircle;
        this.map = map;

        hashMapType = new HashMap<>();
    }

    public void setListener(OnLoadListener<HashMap<String, TrafficType>> listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(HashMap<String, TrafficType> result)
    {
        super.onPostExecute(result);
        listener.onFinish(result);
    }

    @Override
    protected void onProgressUpdate(Object... values)
    {
        super.onProgressUpdate(values);
        Marker marker;
        Log.d("time", "this shit 3");
        if (values.length > 0)
        {
            TrafficType traffic = (TrafficType) values[1];
            Log.d("time", "this shit 4" + traffic.isLine());
            if (traffic.isLine())
            {
                TrafficOption option = (TrafficOption) values[0];
                marker = map.addMarker(option.getMarkerOptions());
                map.addPolyline(option.getPolylineOptions());
            }
            else
            {
                MarkerOptions option = (MarkerOptions) values[0];
                marker = map.addMarker(option);
            }
            hashMapType.put(marker.getId(), traffic);
        }
    }

    @Override
    protected HashMap<String, TrafficType> doInBackground(Integer... params)
    {
        int meta = params[0];
        int mediumColor = params[1];
        int highColor = params[2];
        TrafficLineUtils lineUtils = new TrafficLineUtils(meta, mediumColor, highColor);
        TrafficCircleUtils circleUtils = new TrafficCircleUtils(meta);
        /*int length = listCircle.size() > listLine.size() ? listCircle.size() : listLine.size();
        for (int i=0;i<length;++i)
        {

        }*/

        for (int i = 0; i < listLine.size(); ++i)
        {
            publishProgress(lineUtils.getOption(listLine.get(i)), new TrafficType(true, i));
        }

        for (int i = 0; i < listCircle.size(); ++i)
        {
            publishProgress(circleUtils.getOption(listCircle.get(i)), new TrafficType(false, i));
        }

        return hashMapType;
    }
}
