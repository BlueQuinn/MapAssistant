package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import utils.MapUtils;
import utils.RouteUtils;

/**
 * Created by lequan on 10/2/2016.
 */
public class Route implements SafeParcelable

{
    ArrayList<Path> paths;

    public Path getPath(int index)
    {
        return paths.get(index);
    }

    public Route(ArrayList<Path> paths)
    {
        this.paths = paths;
        distance = 0;
        duration = 0;
        for (Path i : paths)
        {
            distance += i.getDistance();
            duration += i.getDuration();
        }
    }

    public ArrayList<LatLng> getRoute()
    {
        ArrayList<LatLng> route = new ArrayList<>();
        for (Path i : paths)
        {
            route.addAll(i.getPath());
        }
        return route;
    }

    public int pathCount()
    {
        return paths.size();
    }

    public String getInfo()
    {
        return RouteUtils.getInformation(duration, distance);
    }

    public int getDistance()
    {
        return distance;
    }

    public int getDuration()
    {
        return duration;
    }



    int duration;       // seconds
    int distance;       // meters

    public boolean inCircle(LatLng center, double radius)
    {
        if (MapUtils.distance(paths.get(0).getStart(), center) > radius)
        {
            return false;
        }
        for (Path i : paths)
        {
            if (MapUtils.distance(i.getEnd(), center) > radius)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeList(paths);
        dest.writeInt(duration);
        dest.writeInt(distance);
    }

    Route(Parcel in)
    {
        paths = in.readArrayList(Route.class.getClassLoader());
        duration = in.readInt();
        distance = in.readInt();
    }

    public static final SafeParcelable.Creator CREATOR = new SafeParcelable.Creator()
    {
        public Route createFromParcel(Parcel in)
        {
            return new Route(in);
        }

        public Route[] newArray(int size)
        {
            return new Route[size];
        }
    };
}
