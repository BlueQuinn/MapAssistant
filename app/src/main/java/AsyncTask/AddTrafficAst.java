package AsyncTask;

import android.location.Geocoder;
import android.os.AsyncTask;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import DTO.Traffic;
import Listener.OnLoadListener;
import Utils.AddressUtils;

/**
 * Created by lequan on 8/29/2016.
 */
public class AddTrafficAst extends AsyncTask<Integer, MarkerOptions, Boolean>
{
    ArrayList<Traffic> listTraffic;
    OnLoadListener<Boolean> listener;
    GoogleMap map;
    Geocoder geocoder;

    public AddTrafficAst(ArrayList<Traffic> listTraffic, GoogleMap map, Geocoder geocoder)
    {
        this.listTraffic = listTraffic;
        this.map = map;
        this.geocoder = geocoder;
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
        BitmapDescriptor icon;
        MarkerOptions options;
        int meta = params[0];
        String level;
        for (Traffic traffic : listTraffic)
        {
            if (traffic.getVote() < 2 * meta)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_medium);
                level = "\nÙn tắc giao thông";
            }
            else
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_high);
                level = "\nKẹt xe";
            }
            options = new MarkerOptions().icon(icon);
            options.position(new LatLng(traffic.getLat(), traffic.getLng())).title(AddressUtils.getAddress(geocoder, traffic.getLat(), traffic.getLng()) + level);
            publishProgress(options);
        }
        return true;
    }
}
