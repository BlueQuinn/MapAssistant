package model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lequan on 10/8/2016.
 */
public class MyTraffic
{
    public LatLng getLocation()
    {
        return location;
    }

    int id;
    LatLng location;

    public int getRadius()
    {
        return radius;
    }

    public String getAddress()
    {
        return address;
    }

    public MyTraffic(int id, double lat, double lng, int radius, String time, String address)
    {
        this.id = id;
        this.location = new LatLng(lat, lng);
        this.radius = radius;
        this.time = time;
        this.address = address;
    }

    int radius;
    String time, address;

    public int getId()
    {
        return id;
    }

    public String getTime()
    {
        return time;
    }

}

/*public class MyTraffic implements SafeParcelable
{
    public LatLng getLocation()
    {
        return location;
    }

    public MyTraffic(LatLng location, int id, String time)
    {

        this.location = location;
        this.id = id;
        this.time = time;
    }

    LatLng location;
    int id;
    String time;

    public int getId()
    {
        return id;
    }

    public String getTime()
    {
        return time;
    }

    public MyTraffic(double lat, double lng, int id, String time)
    {
        location = new LatLng(lat, lng);
        this.id = id;
        this.time = time;
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

    MyTraffic(Parcel in) {
        mData = in.readInt();
    }

    public static final SafeParcelable.Creator<MyTraffic> CREATOR
            = new SafeParcelable.Creator<MyTraffic>() {
        public MyTraffic createFromParcel(Parcel in) {
            return new MyTraffic(in);
        }

        public MyTraffic[] newArray(int size) {
            return new MyTraffic[size];
        }
    };
}*/
