package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import utils.PolyUtils;
import utils.RouteUtils;

/**
 * Created by lequan on 10/8/2016.
 */
public class Shortcut implements SafeParcelable
{
    int id;
    String routeString;
    int rating;
    int duration, distance;
    ArrayList<LatLng> route;

    public ArrayList<LatLng> getRoute()
    {
        if (route == null)
            route = PolyUtils.decode(routeString);
        return route;
    }

    public LatLng getStart()
    {
        if (route == null)
        {
            route = PolyUtils.decode(routeString);
        }
        return route.get(0);
    }

    public LatLng getEnd()
    {
        if (route == null)
        {
            route = PolyUtils.decode(routeString);
        }
        return route.get(route.size() - 1);
    }

    public String getRouteString()
    {
        return routeString;
    }

    public int getRating()
    {
        return rating;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public Shortcut(int id, String routeString, int rating, int duration, int distance)
    {
        this.id = id;
        this.routeString = routeString;
        this.rating = rating;
        this.duration = duration;
        this.distance = distance;
    }

    public String getInfo()
    {
        return RouteUtils.getInformation(duration, distance);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(routeString);
        dest.writeInt(rating);
        dest.writeInt(duration);
        dest.writeInt(distance);
    }

    public int getId()
    {
        return id;
    }

    Shortcut(Parcel in)

    {
        id = in.readInt();
        routeString = in.readString();
        rating = in.readInt();
        duration = in.readInt();
        distance = in.readInt();
    }

    public static final SafeParcelable.Creator CREATOR = new SafeParcelable.Creator()
    {
        public Shortcut createFromParcel(Parcel in)
        {
            return new Shortcut(in);
        }

        public Shortcut[] newArray(int size)
        {
            return new Shortcut[size];
        }
    };
}
