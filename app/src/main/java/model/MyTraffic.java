package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by lequan on 10/8/2016.
 */
public class MyTraffic implements SafeParcelable
{
    /*public ArrayList<Shortcut> getShortcuts()
    {
        return shortcuts;
    }*/

    public LatLng getCenter()
    {
        return center;

    }

    public int getRadius()
    {
        return radius;
    }

    int id;
    String time, address;
    LatLng center;
    int radius;
    //ArrayList<Shortcut> shortcuts;

    public String getTime()
    {
        return time;
    }

    public String getAddress()
    {
        return address;
    }

    /*public MyTraffic(int id, LatLng center, int radius, String time, String address, ArrayList<Shortcut> shortcuts)
    {
        this.id = id;
        this.center = center;
        this.radius = radius;
        this.time = time;
        this.address = address;
        this.shortcuts = shortcuts;
    }*/

    public MyTraffic(int id, LatLng center, int radius, String time, String address)
    {
        this.id = id;
        this.center = center;
        this.radius = radius;
        this.time = time;
        this.address = address;
    }

    public MyTraffic(int id, LatLng center)
    {
        this.id = id;
        this.center = center;
    }

    public int getId()
    {
        return id;
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
        dest.writeInt(id);
        dest.writeString(time);
        dest.writeString(address);
        //dest.writeTypedList(shortcuts);
    }

    MyTraffic(Parcel in)
    {
        center = in.readParcelable(TrafficCircle.class.getClassLoader());
        radius = in.readInt();
        id = in.readInt();
        time = in.readString();
        address = in.readString();
        //shortcuts = new ArrayList<>();
        //in.readTypedList(shortcuts, Shortcut.CREATOR);
    }

    public static final SafeParcelable.Creator CREATOR = new SafeParcelable.Creator()
    {
        public MyTraffic createFromParcel(Parcel in)
        {
            return new MyTraffic(in);
        }

        public MyTraffic[] newArray(int size)
        {
            return new MyTraffic[size];
        }
    };
}
