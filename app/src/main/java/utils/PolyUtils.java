package utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lequan on 10/6/2016.
 */
public class PolyUtils
{
    public static String encode(List<LatLng> path)
    {
        long lastLat = 0L;
        long lastLng = 0L;
        StringBuffer result = new StringBuffer();

        long lng;
        for (Iterator var6 = path.iterator(); var6.hasNext(); lastLng = lng)
        {
            LatLng point = (LatLng) var6.next();
            long lat = Math.round(point.latitude * 100000.0D);
            lng = Math.round(point.longitude * 100000.0D);
            long dLat = lat - lastLat;
            long dLng = lng - lastLng;
            encode(dLat, result);
            encode(dLng, result);
            lastLat = lat;
        }

        return result.toString();
    }

    static void encode(long v, StringBuffer result)
    {
        for (v = v < 0L ? ~(v << 1) : v << 1; v >= 32L; v >>= 5)
        {
            result.append(Character.toChars((int) ((32L | v & 31L) + 63L)));
        }

        result.append(Character.toChars((int) (v + 63L)));
    }

    public static ArrayList<LatLng> decode(String encodedPath)
    {
        int len = encodedPath.length();
        ArrayList<LatLng> path = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len)
        {
            int result = 1;
            int shift = 0;

            int b;
            do
            {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            }
            while (b >= 31);

            lat += (result & 1) != 0 ? ~(result >> 1) : result >> 1;
            result = 1;
            shift = 0;

            do
            {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            }
            while (b >= 31);

            lng += (result & 1) != 0 ? ~(result >> 1) : result >> 1;
            path.add(new LatLng((double) lat * 1.0E-5D, (double) lng * 1.0E-5D));
        }

        return path;
    }
}
