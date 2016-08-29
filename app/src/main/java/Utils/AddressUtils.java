package Utils;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;

/**
 * Created by lequan on 5/18/2016.
 */
public class AddressUtils
{
    public static String getAddress(Geocoder geocoder, double latitude, double longitude)
    {
        String currentAddress = "";
        try
        {
            Address address = geocoder.getFromLocation(latitude, longitude, 1).get(0);
            for (int i = 0; i < address.getMaxAddressLineIndex() - 1; i++)
            {
                currentAddress += address.getAddressLine(i) + ", ";
            }
            currentAddress += address.getAddressLine(address.getMaxAddressLineIndex() - 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return currentAddress;
    }
}
