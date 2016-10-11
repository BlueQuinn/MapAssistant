package model;

/**
 * Created by lequan on 10/10/2016.
 */
public class WeatherWeek
{
    String date;

    public String getDate()
    {
        return date;
    }

    public String getInfo()
    {
        return info;
    }

    public String getRain()
    {
        return "Lượng mưa: " + rain;
    }

    public int getMaxTemp()
    {
        return maxTemp;
    }

    public int getMinTemp()
    {
        return minTemp;
    }

    public String getTemperature()
    {
        return "Nhiệt độ: " + maxTemp + (char) 0x00B0 + "C - " + minTemp + (char) 0x00B0 + "C";
    }

    String info;
    String rain;
    int maxTemp, minTemp;
    int icon;

    public int getIcon()
    {
        return icon;
    }

    public WeatherWeek(String date, String info, String rain, int maxTemp, int minTemp, int icon)
    {

        this.date = date;
        this.info = info;
        this.rain = rain;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.icon = icon;
    }
}
