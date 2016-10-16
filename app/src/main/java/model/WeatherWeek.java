package model;


import android.util.Log;

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
        this.date = formatDate(date).replace("Tháng", "tháng ");
        this.info = info;
        this.rain = rain;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;

        String up_info = info.toUpperCase();

        if (up_info.contains("Mưa") || up_info.contains("MƯA LỚN"))
            icon = R.drawable.heavyrain;
        else if (up_info.contains("MƯA") && up_info.contains("NẮNG"))
            icon = R.drawable.sun_rain;
        else if (up_info.contains("NẮNG") && up_info.contains("MÂY"))
            icon = R.drawable.sun_clound;
        else if (up_info.contains("SẤM") || up_info.contains("CHỐP"))
            icon = R.drawable.thunder;
        else if (up_info.contains("MÂY"))
            icon = R.drawable.cloud;
        else if (up_info.contains("MƯA"))
            icon = R.drawable.rain;
        else if (up_info.contains("NẮNG"))
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
        return "Nhiệt độ: " + minTemp + (char) 0x00B0 + "C - " + maxTemp + (char) 0x00B0 + "C";
    }

    private String formatDate(String time) {
        time =  time.replace("Thg","Tháng");
        Log.d("Son","time: " + time);
        for (int i = 0; i < time.length(); i++) {
            char c = time.charAt(i);
            if (c >= '0' && c <= '9') {
                time = time.substring(0, i) + " " + time.substring(i);
                Log.d("Son","time final: " + time);
                return time;
            }
        }

        return time;
    }
}
