package model;

import java.util.ArrayList;

/**
 * Created by lequan on 10/11/2016.
 */
public class Traffic
{
    ArrayList<TrafficLine> listLine;
    ArrayList<TrafficCircle> listCircle;

    public ArrayList<TrafficLine> getListLine()
    {
        return listLine;
    }

    public ArrayList<TrafficCircle> getListCircle()
    {
        return listCircle;
    }

    public Traffic(ArrayList<TrafficLine> listLine, ArrayList<TrafficCircle> listCircle)
    {

        this.listLine = listLine;
        this.listCircle = listCircle;
    }
}
