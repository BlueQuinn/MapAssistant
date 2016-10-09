package utils;

/**
 * Created by lequan on 10/8/2016.
 */
public class TimeUtils
{

    public static int toMinutes(String time)
    {
        String[] hourMin = time.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int mins = Integer.parseInt(hourMin[1]);
        int hoursInMins = hour * 60;
        return hoursInMins + mins;
    }

    public static  String toTime(int minutes)
    {
        int hour = minutes / 60;
        minutes = minutes - hour * 60;
        return Integer.toString(hour) + ":" + Integer.toString(minutes);
    }
}
