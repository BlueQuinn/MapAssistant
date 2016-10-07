package utils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.ArrayList;

import asyncTask.DirectionAst;
import asyncTask.FindPlaceAst;
import listener.OnLoadListener;
import model.Place;
import model.Route;

/**
 * Created by lequan on 5/18/2016.
 */
public class MapUtils
{
    public static void getRoad(String apiKey, final LatLng point, final int length, final OnLoadListener<LatLng[]> listener)
    {
        FindPlaceAst asyncTask = new FindPlaceAst(apiKey, "cafe", 1);
        asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<Place>>()
        {
            @Override
            public void onFinish(final ArrayList<Place> list)
            {
                if (list.size() > 1)
                {
                    final DirectionAst directionAst = new DirectionAst();
                    directionAst.setOnLoadListener(new OnLoadListener<Route>()
                    {
                        @Override
                        public void onFinish(Route route)
                        {
                            for (int i = 0; i < route.pathCount(); ++i)
                            {
                                LatLng[] path = route.getPath(i);
                                ArrayList<LatLng> polyline = new ArrayList<>();
                                polyline.add(path[0]);
                                polyline.add(path[1]);
                                if (PolyUtils.isLocationOnPath(point, polyline, false, 200))
                                {
                                    LatLng[] intersection = MathUtils.getIntersection(path[0], path[1], point, length);
                                    listener.onFinish(intersection);
                                    //listener.onFinish(new LatLng[]{path[0], path[1]});

                                    Log.d("123", ""+intersection);
                                    Log.d("123", ""+path[0] + "   " + path[1]);
                                    return;
                                }
                            }
                        }
                    });
                    //LatLng A = new LatLng(list.get(0).getLat(), list.get(0).getLng());
                    //LatLng B = new LatLng(list.get(1).getLat(), list.get(1).getLng());
                    LatLng A = new LatLng(10.76353877327849, 106.68203115463257);
                    LatLng B = new LatLng(10.834486195704075, 106.67218208312988);
                    Log.d("123", ""+ A + "   " + B);
                    directionAst.execute(A, B, point);
                }
                else
                {
                    listener.onFinish(null);
                }
            }
        });
        asyncTask.execute(point.latitude, point.longitude);
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
