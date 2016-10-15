package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import utils.MapUtils;

/**
 * Created by lequan on 5/14/2016.
 */
public class TrafficLine implements SafeParcelable
{
    int id;

    public int getId()
    {
        return id;
    }

    LatLng start, end;
    int rating;

    public int getRating()
    {
        return rating;
    }

    public LatLng getStart()
    {
        return start;
    }

    public LatLng getEnd()
    {
        return end;
    }

    public double length()
    {
        return MapUtils.distance(start, end);
    }

    public TrafficLine(int id, double lat1, double lng1, double lat2, double lng2, int rating, ArrayList<Shortcut> shortcuts)
    {
        this.id = id;
        start = new LatLng(lat1, lng1);
        end = new LatLng(lat2, lng2);
        this.rating = rating;

        this.shortcuts = shortcuts;
    }

    public LatLng getCenter()
    {
        return new LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2);
    }

    public LatLng intersect(LatLng A, LatLng B)
    {
        return null;
    }

    public ArrayList<LatLng> getPolyline()
    {
        ArrayList<LatLng> list = new ArrayList<>();
        list.add(start);
        list.add(end);
        return list;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(start, flags);
        dest.writeParcelable(end, flags);
        dest.writeInt(rating);
        dest.writeTypedList(shortcuts);
    }

    TrafficLine(Parcel in)
    {
        start = in.readParcelable(TrafficCircle.class.getClassLoader());
        end = in.readParcelable(TrafficCircle.class.getClassLoader());
        rating = in.readInt();
        shortcuts = new ArrayList<>();
        in.readTypedList(shortcuts, Shortcut.CREATOR);
    }

    public static final SafeParcelable.Creator CREATOR = new SafeParcelable.Creator()
    {
        public TrafficLine createFromParcel(Parcel in)
        {
            return new TrafficLine(in);
        }

        public TrafficLine[] newArray(int size)
        {
            return new TrafficLine[size];
        }
    };


    ArrayList<Shortcut> shortcuts;

    public ArrayList<Shortcut> getShortcuts()
    {
        return shortcuts;
    }
}