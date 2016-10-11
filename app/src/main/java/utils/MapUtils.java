package utils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.ArrayList;

import asyncTask.DirectionAst;
import asyncTask.FindPlaceAst;
import listener.DetectTrafficListener;
import listener.OnLoadListener;
import model.Path;
import model.Place;
import model.Route;

/**
 * Created by lequan on 5/18/2016.
 */
public class MapUtils
{
    double length;

    public MapUtils(int length)
    {
        this.length = length;
    }

    public void getRoad(String apiKey, final LatLng point, final DetectTrafficListener listener)
    {
        FindPlaceAst asyncTask = new FindPlaceAst(apiKey, "cafe", 2);
        asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<Place>>()
        {
            @Override
            public void onFinish(final ArrayList<Place> list)
            {
                if (list.size() > 1)
                {
                    LatLng A = null;
                    LatLng B = null;
                    for (int i = 0; i < list.size() - 1; ++i)
                    {
                        for (int j = i + 1; j < list.size(); ++j)
                        {
                            LatLng a = new LatLng(list.get(i).getLat(), list.get(i).getLng());
                            LatLng b = new LatLng(list.get(j).getLat(), list.get(j).getLng());
                            if (distance(a, b) > 2000)      // not so sure about 2000
                            {
                                A = a;
                                B = b;
                                break;
                            }
                        }
                        if (A!= null)
                            break;
                    }

                    if (A == null)      // or B == null
                    {
                        listener.onFinish(null, 0, null);
                        return;
                    }

                    final DirectionAst directionAst = new DirectionAst();
                    directionAst.setOnLoadListener(new OnLoadListener<Route>()
                    {
                        @Override
                        public void onFinish(Route route)
                        {
                            for (int i = 0; i < route.pathCount(); ++i)
                            {
                                Path path = route.getPath(i);
                                if (path.getDistance() > 200 && PolyUtils.isLocationOnPath(point, path.getPath(), false, 100))
                                {
                                    double diff1 = distance(point, path.getStart());
                                    double diff2 = distance(point, path.getEnd());
                                    if (length > diff1 && length > diff2)
                                    {
                                        listener.onFinish(null, 0, null);
                                        return;
                                    }

                                    if (length >= diff1)
                                    {
                                        listener.onFinish(route, i, findIntersection(path.getStart(), point));
                                        //listener.onDetect(route, i);
                                        return;
                                    }
                                    if (length >= diff2)
                                    {
                                        listener.onFinish(route, i, findIntersection(path.getEnd(), point));
                                        //listener.onDetect(route, i);
                                        return;
                                    }

                                    if (length < diff1 && length < diff2)
                                    {
                                        LatLng[] intersection = MathUtils.getIntersection(path.getStart(), path.getEnd(), point, length);
                                        listener.onFinish(route, i, intersection);
                                        //listener.onFinish(new LatLng[]{path[0], path[1]});
                                        Log.d("123", "" + intersection);
                                        Log.d("123", "" + path.getStart() + "   " + path.getEnd());
                                    }
                                    return;
                                }
                            }
                        }
                    });
                    //LatLng A = new LatLng(10.76353877327849, 106.68203115463257);
                    //LatLng B = new LatLng(10.834486195704075, 106.67218208312988);
                    Log.d("123", "" + A + "   " + B);
                    directionAst.execute(A, B, point);
                }
                else
                {
                    listener.onFinish(null,0,null);
                }
            }
        });
        asyncTask.execute(point.latitude, point.longitude);
    }

    static LatLng[] findIntersection(LatLng A, LatLng center)
    {
        LatLng B = new LatLng(2 * center.latitude - A.latitude, 2 * center.longitude - A.longitude);
        return new LatLng[]{A, B};
    }

    static double distance(LatLng A, LatLng B)       // not so sure about this
    {
        //double lat_a, double lng_a, double lat_b, double lng_b
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(B.latitude - A.latitude);
        double lngDiff = Math.toRadians(B.longitude - A.longitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(A.latitude)) * Math.cos(Math.toRadians(B.latitude)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }

    public static LatLngBounds getBound(LatLng... point)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng i : point)
        {
            builder.include(i);
        }
        return builder.build();
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

    static int findFirstNumber(String s)
    {
        for (int i = 0; i < s.length(); ++i)
        {
            if (s.charAt(i) >= '0' && s.charAt(i) <= '9')
            {
                return i;
            }
        }
        return -1;
    }
}
