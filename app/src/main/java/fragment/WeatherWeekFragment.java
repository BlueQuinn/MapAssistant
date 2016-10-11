package fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.ArrayList;

import adapter.DestinationAdt;
import adapter.WeatherWeekAdt;
import model.WeatherWeek;

/**
 * Created by lequan on 10/10/2016.
 */
public class WeatherWeekFragment extends Fragment
{
    ListView listView;
    ArrayList<WeatherWeek> listWeather;
    WeatherWeekAdt adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View convertView = inflater.inflate(R.layout.fragment_weather_week, container, false);

        //prbLoading = (ProgressBar) convertView.findViewById(R.id.prbLoading);

        listWeather = new ArrayList<>();
        listWeather.add(new WeatherWeek("Hôm nay    10/10", "Có mây và mưa kèm theo khả năng có giông bão lớn", "22,5mm", 24, 31, R.drawable.storm));
        listWeather.add(new WeatherWeek("Thứ Ba    11/10", "Mưa", "22,5mm", 25, 32, R.drawable.rain));
        listWeather.add(new WeatherWeek("Thứ Tư    12/10", "Mưa và có thể có sấm", "22,5mm", 24, 32, R.drawable.storm));
        listWeather.add(new WeatherWeek("Thứ Năm    13/10", "Nắng to", "22,5mm", 20, 35, R.drawable.sun));
        listWeather.add(new WeatherWeek("Thứ Sáu    14/10", "Nắng dịu", "22,5mm", 27, 31, R.drawable.sun));
        listWeather.add(new WeatherWeek("Thứ Bảy    15/10", "Có thể có mưa", "22,5mm", 29, 35, R.drawable.rain));
        listWeather.add(new WeatherWeek("Chủ Nhật    16/10", "Trời dịu nhẹ, không mây", "22,5mm", 26, 34, R.drawable.normal_cloud));

        adapter = new WeatherWeekAdt(getActivity().getApplicationContext(), R.layout.row_weather_week, listWeather);
        listView = (ListView) convertView.findViewById(R.id.lvWeather);
        listView.setAdapter(adapter);

        //loadDestination();

        return convertView;
    }
}
