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

import adapter.WeatherWeekAdt;
import asyncTask.WeatherWeekAst;
import listener.OnLoadListener;
import model.WeatherWeek;

/**
 * Created by lequan on 10/10/2016.
 */
public class WeatherWeekFragment extends Fragment
{
    ListView listView;
    ArrayList<WeatherWeek> listWeather;
    WeatherWeekAdt adapter;
    ProgressBar prbLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View convertView = inflater.inflate(R.layout.fragment_weather, container, false);

        prbLoading = (ProgressBar) convertView.findViewById(R.id.prbLoading);
        listView = (ListView) convertView.findViewById(R.id.lvWeather);
        listView.setVisibility(View.GONE);
        prbLoading.setVisibility(View.VISIBLE);

        listWeather = new ArrayList<>();
        adapter = new WeatherWeekAdt(getActivity().getApplicationContext(), R.layout.row_weather_week, listWeather);
        listView.setAdapter(adapter);
        WeatherWeekAst asyncTask = new WeatherWeekAst();
        asyncTask.setListener(new OnLoadListener<ArrayList<WeatherWeek>>()
        {
            @Override
            public void onFinish(ArrayList<WeatherWeek> weatherTimes)
            {
                prbLoading.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                for (WeatherWeek i : weatherTimes)
                    listWeather.add(i);
                adapter.notifyDataSetChanged();
            }
        });
        asyncTask.execute();
        return convertView;
    }
}
