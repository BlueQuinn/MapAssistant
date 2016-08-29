package DTO;

/**
 * Created by lequan on 5/14/2016.
 */
public class Position
{
    double lat, lng;

    public double getLat()
    {
        return lat;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public double getLng()
    {
        return lng;
    }

    public void setLng(double lng)
    {
        this.lng = lng;
    }

    public Position(double lat, double lng)
    {

        this.lat = lat;
        this.lng = lng;
    }
}
