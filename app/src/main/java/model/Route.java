package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

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

    public String getInformation()
    {
        return getDuration(duration) + " - " + getDistance(distance);
    }

    public int getDistance()
    {
        return distance;
    }

    public int getDuration()
    {
        return duration;
    }

    String getDuration(int duration)
    {
        String time;
        int hour = duration / 3600;
        int minute = (duration - hour * 3600) / 60;
        if (hour == 0)
        {
            time = Integer.toString(minute) + "p";
        }
        else
        {
            time = Integer.toString(hour) + "h";
            if (minute != 0)
            {
                time += Integer.toString(minute) + "p";
            }
        }
        return time;
    }

    String getDistance(int distance)
    {
        String dtc;
        if (distance < 1000)
        {
            dtc = Integer.toString(distance) + "m";
        }
        else
        {
            dtc = Float.toString(Math.round(distance * 1000) / 1000) + "km";
        }

        return dtc;
    }

    int duration;       // seconds
    int distance;       // meters

    public boolean inCircle(LatLng center, double radius)
    {
        if (distance(paths.get(0).getStart(), center) > radius)
        {
            return false;
        }
        for (Path i : paths)
        {
            if (distance(i.getEnd(), center) > radius)
            {
                return false;
            }
        }
        return true;
    }

    public static double distance(LatLng A, LatLng B)
    {
        return 1.0 * Math.sqrt((A.latitude * A.latitude - B.latitude * B.latitude) + (A.longitude * A.longitude - B.longitude * B.longitude));
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
