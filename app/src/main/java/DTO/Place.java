package DTO;

/**
 * Created by lequan on 5/15/2016.
 */
public class Place
{
    double lat,lng;
    String name, address;

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

    public Place(double lat, double lng, String name, String address)
    {

        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.address = address;
    }
}
