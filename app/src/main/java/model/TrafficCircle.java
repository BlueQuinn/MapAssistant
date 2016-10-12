package model;

import android.os.Parcel;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lequan on 10/11/2016.
 */
public class TrafficCircle implements SafeParcelable
{
    public static int getRadius(int radius)
    {
        return 50 * (radius + 4);
    }

    LatLng center;

    public LatLng getCenter()
    {
        return center;
    }

    public TrafficCircle(LatLng center, int radius, int rating)
    {

        this.center = center;
        this.radius = radius;
        this.rating = rating;
    }

    int radius, rating;


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
    }

    TrafficCircle(Parcel in)
    {
        center = in.readParcelable(TrafficCircle.class.getClassLoader());
        radius = in.readInt();
        rating = in.readInt();
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
}
