package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by lequan on 10/8/2016.
 */
public class Path implements SafeParcelable
{
    ArrayList<LatLng> path;

    public LatLng getStart()
    {
        if (path.size() > 0)
        {
            return path.get(0);
        }
        return null;
    }

    public LatLng getEnd()
    {
        if (path.size() > 0)
        {
            return path.get(path.size() - 1);
        }
        return null;
    }

    public void addAll(ArrayList<LatLng> points)
    {
        path.addAll(points);
    }

    public void add(LatLng point)
    {
        path.add(point);
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public void setDistance(int distance)
    {
        this.distance = distance;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public ArrayList<LatLng> getPath()
    {
        return path;
    }

    public Path()
    {
        path = new ArrayList<>();
    }


    int duration;       // seconds
    int distance;       // meters

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeList(path);
        dest.writeInt(duration);
        dest.writeInt(distance);
    }

    Path(Parcel in)
    {
        path = in.readArrayList(Path.class.getClassLoader());
        duration = in.readInt();
        distance = in.readInt();
    }

    public static final SafeParcelable.Creator CREATOR = new SafeParcelable.Creator()
    {
        public Path createFromParcel(Parcel in)
        {
            return new Path(in);
        }

        public Path[] newArray(int size)
        {
            return new Path[size];
        }
    };
}
