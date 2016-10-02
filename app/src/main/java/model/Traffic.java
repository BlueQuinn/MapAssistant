package model;

/**
 * Created by lequan on 5/14/2016.
 */
public class Traffic
{
    double lat, lng;
    int vote;

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public int getVote()
    {
        return vote;
    }

    public Traffic(double lat, double lng, int vote)
    {
        this.lat = lat;
        this.lng = lng;
        this.vote = vote;
    }
}