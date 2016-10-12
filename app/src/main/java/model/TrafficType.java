package model;

/**
 * Created by lequan on 10/12/2016.
 */
public class TrafficType
{
    boolean type;
    int index;

    public boolean isLine()
    {
        return type;
    }

    public int getIndex()
    {
        return index;
    }

    public TrafficType(boolean type, int index)
    {

        this.type = type;
        this.index = index;
    }
}
