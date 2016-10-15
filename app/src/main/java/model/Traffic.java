package model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lequan on 10/11/2016.
 */
public class Traffic
{
    HashMap<String, TrafficLine> listLine;
    HashMap<String, TrafficCircle> listCircle;


    public Traffic()
    {
        listLine = new HashMap<>();
        listCircle = new HashMap<>();
    }

    public boolean isLine(String id)
    {
        return listLine.get(id) != null;
    }

    public TrafficCircle getCircle(String id)
    {
        return listCircle.get(id);
    }

    public TrafficLine getLine(String id)
    {
        return listLine.get(id);
    }

    public void addCircle(String id, TrafficCircle circle)
    {
        listCircle.put(id, circle);
    }

    public void addLine(String id, TrafficLine line)
    {
        listLine.put(id, line);
    }

    public static final String MY_TRAFFIC = "my_traffic";
    public static final String CIRCLE = "circle";
    public static final String LINE = "line";

    public ArrayList<TrafficLine> getLine()
    {
        ArrayList<TrafficLine> list = new ArrayList<>();
        ArrayList<String> key = new ArrayList<>(listLine.keySet());
        for (String i : key)
        {
            list.add(listLine.get(i));
        }
return list;
    }

    public ArrayList<TrafficCircle> getCircle()
    {
        ArrayList<TrafficCircle> list = new ArrayList<>();
        ArrayList<String> key = new ArrayList<>(listCircle.keySet());
        for (String i : key)
        {
            list.add(listCircle.get(i));
        }
        return list;
    }
}
