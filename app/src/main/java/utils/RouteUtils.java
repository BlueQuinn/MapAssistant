package utils;

/**
 * Created by lequan on 10/12/2016.
 */
public class RouteUtils
{
    public static String getInformation(int duration, int distance)
    {
        return getDuration(duration) + " - " + getDistance(distance);
    }

    public static String getDuration(int duration)
    {
        String time;
        int hour = duration / 3600;
        int minute = (duration - hour * 3600) / 60;
        if (hour == 0)
        {
            return Integer.toString(minute) + " phút";
        }
        else
        {
            if (minute == 0)
            {
                return Integer.toString(duration) + " giây";
            }
            else
            {
                return Integer.toString(hour) + " tiếng" +  Integer.toString(minute) + " phút";
            }
        }
    }

    public static String getDistance(int distance)
    {
        String dtc;
        if (distance < 1000)
        {
            dtc = Integer.toString(distance) + " mét";
        }
        else
        {
            dtc = Float.toString(Math.round(distance * 1000) / 1000) + "km";
        }
        return dtc;
    }
}
