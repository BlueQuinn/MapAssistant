package model;

/**
 * Created by lequan on 8/30/2016.
 */
public class Nearby
{
    String placeType;
    int radius;

    public String getPlaceType()
    {
        return placeType;
    }

    public int getRadius()
    {
        return radius;
    }

    public Nearby(String placeType, int radius)
    {

        this.placeType = placeType;
        this.radius = radius;
    }
}
