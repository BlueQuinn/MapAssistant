package asyncTask;

import android.location.Geocoder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import model.Traffic;

/**
 * Created by lequan on 8/29/2016.
 */
public class FindTrafficAst extends AddTrafficAst
{

    public FindTrafficAst(ArrayList<Traffic> listTraffic, GoogleMap map, Geocoder geocoder)
    {
        super(listTraffic, map);
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

}
