package model;

import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by SonPham on 10/14/2016.
 */
public class WeatherTime {
    String time, info, rain, humidity ;
    int temperature, icon;

    public WeatherTime(String time, String info, String rain, String humidity, int temperature){
        this.time = time;
        this.info = info;
        this.rain = rain;
        this.humidity = humidity;
        this.temperature = temperature;

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

    public String getTime() {
        return time;
    }

    public String getInfo() {
        return info;
    }

    public String getRain() {
        return "Lượng mưa: " + rain;
    }

    public String getHumidity() {
        return "Độ ẩm: " + humidity;
    }

    public String getTemperature() {
        return "Nhiệt độ: " + temperature + (char) 0x00B0 + "C";
    }

    public int getIcon() {
        return icon;
    }
}
