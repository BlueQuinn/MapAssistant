package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by lequan on 10/11/2016.
 */
public class TrafficCircle implements SafeParcelable
{
    int id;

    public int getId()
    {
        return id;
    }

    public TrafficCircle(int id, LatLng center, int radius, int rating, ArrayList<Shortcut> shortcuts)
    {
        this.id = id;

        this.center = center;
        this.radius = radius;
        this.rating = rating;

        this.shortcuts = shortcuts;
    }

    public TrafficCircle()
    {
    }

    public static int getRadius(int radius)
    {
        return radius;
    }

    LatLng center;

    public LatLng getCenter()
    {
        return center;
    }

    int radius;
    private int rating;


    public int getRadius()
    {
        return 50 * (radius + 4);
    }

    public int getRating()
    {
        return rating;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(center, flags);
        dest.writeInt(radius);
        dest.writeInt(rating);
        dest.writeTypedList(shortcuts);
    }

    TrafficCircle(Parcel in)
    {
        center = in.readParcelable(TrafficCircle.class.getClassLoader());
        radius = in.readInt();
        rating = in.readInt();
        shortcuts = new ArrayList<>();
        in.readTypedList(shortcuts, Shortcut.CREATOR);
    }

    public static final SafeParcelable.Creator CREATOR = new SafeParcelable.Creator()
    {
        public TrafficCircle createFromParcel(Parcel in)
        {
            return new TrafficCircle(in);
        }

        public TrafficCircle[] newArray(int size)
        {
            return new TrafficCircle[size];
        }
    };

    ArrayList<Shortcut> shortcuts;

    public ArrayList<Shortcut> getShortcuts()
    {
        return shortcuts;
    }
}
