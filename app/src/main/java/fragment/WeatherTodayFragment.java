package fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.ArrayList;

import adapter.WeatherWeekAdt;
import model.WeatherWeek;

/**
 * Created by lequan on 10/10/2016.
 */
public class WeatherTodayFragment extends Fragment
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
        listWeather.add(new WeatherWeek("Sáng", "Nắng to", "22,5mm", 24, 31, R.drawable.sun));
        listWeather.add(new WeatherWeek("Chiều", "Mưa", "22,5mm", 25, 32, R.drawable.rain));
        listWeather.add(new WeatherWeek("Tối", "Mưa và có thể có sấm", "22,5mm", 24, 32, R.drawable.storm));
        listWeather.add(new WeatherWeek("Đêm", "Có mây và mưa kèm theo khả năng có giông bão lớn", "22,5mm", 20, 35, R.drawable.normal_cloud));

        adapter = new WeatherWeekAdt(getActivity().getApplicationContext(), R.layout.row_weather_week, listWeather);
        listView = (ListView) convertView.findViewById(R.id.lvWeather);
        listView.setAdapter(adapter);

        //loadDestination();

        return convertView;
    }
}
