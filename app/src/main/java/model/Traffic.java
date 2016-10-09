package model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lequan on 5/14/2016.
 */
public class Traffic
{
  LatLng start, end;
    int vote;

    public int getVote()
    {
        return vote;
    }

    public LatLng getStart()
    {
        return start;
    }

    public LatLng getEnd()
    {
        return end;
    }

    /*public Traffic(LatLng start, LatLng end, int vote)
    {
        this.start = start;
        this.end = end;
        this.vote = vote;
    }
*/
    public Traffic(double lat1, double lng1, double lat2, double lng2, int vote)
    {
        start = new LatLng(lat1, lng1);
        end = new LatLng(lat2, lng2);
        this.vote = vote;
    }

    public LatLng getCenter()
    {
        return new LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2);
    }

    public LatLng intersect(LatLng A, LatLng B)
    {
        return null;
    }
}