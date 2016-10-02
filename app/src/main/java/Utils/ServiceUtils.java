package utils;

import android.content.Context;
import android.location.LocationManager;

/**
 * Created by lequan on 9/2/2016.
 */
public class ServiceUtils
{
    public static boolean checkServiceEnabled(Context context)
    {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }
}
