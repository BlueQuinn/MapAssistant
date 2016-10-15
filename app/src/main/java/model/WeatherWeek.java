package model;


import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by SonPham on 10/13/2016.
 */
public class WeatherWeek {

    String date;
    String info;
    String rain;
    int icon;
    int maxTemp;
    int minTemp;

    public WeatherWeek(String date, String info, String rain, int minTemp, int maxTemp)
    {
        this.date = date;
        this.info = info;
        this.rain = rain;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;


        if (info.contains("mưa to") || info.contains("mưa lớn"))
            icon = R.drawable.heavyrain;
        else if (info.contains("mưa") && info.contains("nắng"))
            icon = R.drawable.sun_rain;
        else if (info.contains("nắng") && info.contains("mây"))
            icon = R.drawable.sun_clound;
        else if (info.contains("sấm") || info.contains("chốp"))
            icon = R.drawable.thunder;
        else if (info.contains("mây"))
            icon = R.drawable.cloud;
        else if (info.contains("mưa"))
            icon = R.drawable.rain;
        else if (info.contains("nắng"))
            icon = R.drawable.sun;
    }

    public String getDate() {
        return date;
    }

    public String getInfo() {
        return info;
    }

    public String getRain() {
        return rain;
    }

    public int getIcon() {
        return icon;
    }

    public String getTemperature()
    {
        return "Nhiệt độ: " + maxTemp + (char) 0x00B0 + "C - " + minTemp + (char) 0x00B0 + "C";
    }
}
