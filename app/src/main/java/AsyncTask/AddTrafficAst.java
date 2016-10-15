package asyncTask;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import model.Traffic;
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
public class AddTrafficAst extends AsyncTask<Integer, Object, Traffic>
{
    ArrayList<TrafficLine> listLine;
    ArrayList<TrafficCircle> listCircle;
    OnLoadListener<Traffic> listener;
    GoogleMap map;
    Traffic hmTraffic;

    public AddTrafficAst(ArrayList<TrafficLine> listLine, ArrayList<TrafficCircle> listCircle, GoogleMap map)
    {
        this.listLine = listLine;
        this.listCircle = listCircle;
        this.map = map;
    }

    public void setListener(OnLoadListener<Traffic> listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Traffic result)
    {
        super.onPostExecute(result);
        listener.onFinish(result);
    }

    @Override
    protected void onProgressUpdate(Object... values)
    {
        super.onProgressUpdate(values);
        Marker marker;
        //Log.d("time", "this shit 3");
        if (values.length > 0)
        {
            String type = (String) values[1];
            //Log.d("time", "this shit 4" + traffic.isLine());
            if (type.equals(Traffic.LINE))
            {
                TrafficOption option = (TrafficOption) values[0];
                marker = map.addMarker(option.getMarkerOptions());
                map.addPolyline(option.getPolylineOptions());
                hmTraffic.addLine(marker.getId(), listLine.get((int) values[2]));
            }
            else
            {
                MarkerOptions option = (MarkerOptions) values[0];
                marker = map.addMarker(option);
                hmTraffic.addCircle(marker.getId(), listCircle.get((int) values[2]));
            }

            //hashMapType.put(marker.getId(), traffic);
        }
    }

    @Override
    protected Traffic doInBackground(Integer... params)
    {
        int meta = params[0];
        int mediumColor = params[1];
        int highColor = params[2];
        TrafficLineUtils lineUtils = new TrafficLineUtils(meta, mediumColor, highColor);
        TrafficCircleUtils circleUtils = new TrafficCircleUtils(meta);
        hmTraffic = new Traffic();
        for (int i = 0; i < listLine.size(); ++i)
        {
            publishProgress(lineUtils.getOption(listLine.get(i)), Traffic.LINE, i);
        }

        for (int i = 0; i < listCircle.size(); ++i)
        {
            publishProgress(circleUtils.getOption(listCircle.get(i)), Traffic.CIRCLE, i);
        }

        return hmTraffic;
    }
}
