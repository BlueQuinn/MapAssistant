package model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import utils.PolyUtils;

/**
 * Created by lequan on 10/8/2016.
 */
public class Shortcut
{
    double lat,lng;
    String route;
    int rate;
    int duration, distance;

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public String getRoute()
    {
        return route;
    }

    public ArrayList<LatLng> getPolyRoute()
    {
        return PolyUtils.decode(route);
    }

    public int getRate()
    {
        return rate;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public Shortcut(double lat, double lng, String route, int rate, int duration, int distance)
    {
        this.lat = lat;
        this.lng = lng;
        this.route = route;
        this.rate = rate;
        this.duration = duration;
        this.distance = distance;
    }
}

/*public class Shortcut implements SafeParcelable
{
    double lat,lng;
    String route;
    int rate;
    int duration, distance;

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public ArrayList<LatLng> getPolyRoute()
    {
        return PolyUtils.decode(route);
    }

    public int getRate()
    {
        return rate;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public Shortcut(double lat, double lng, String route, int rate, int duration, int distance)
    {
        this.lat = lat;
        this.lng = lng;
        this.route = route;
        this.rate = rate;
        this.duration = duration;
        this.distance = distance;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

    }

    int mData;

    Shortcut(Parcel in) {
        mData = in.readInt();
    }

    public static final SafeParcelable.Creator<Shortcut> CREATOR
            = new SafeParcelable.Creator<Shortcut>() {
        public Shortcut createFromParcel(Parcel in) {
            return new Shortcut(in);
        }

        public Shortcut[] newArray(int size) {
            return new Shortcut[size];
        }
    };
}*/
