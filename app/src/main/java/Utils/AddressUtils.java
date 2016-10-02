package utils;

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

    public static String minimizeAddress(String address)
    {
        int comma = address.indexOf(',');
        if (comma < 1)
        {
            comma = address.length();
        }
        int splash = address.indexOf('/');
        if (splash == -1)
        {
            address = address.substring(findFirstNumber(address), comma);
        }
        else
        {
            int i = address.indexOf(' ');
            if (i > splash + 1)
            {
                String s = address.substring(findFirstNumber(address), splash) + address.substring(i, comma);
                address = s;
            }
        }
        return address;
    }

    static int  findFirstNumber(String s)
    {
        for (int i = 0 ;i<s.length(); ++i)
            if(s.charAt(i) >= '0' && s.charAt(i) <= '9')
                return i;
        return -1;
    }
}
