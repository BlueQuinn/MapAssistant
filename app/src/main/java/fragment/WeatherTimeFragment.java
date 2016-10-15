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

import adapter.WeatherTimeAdt;
import asyncTask.WeatherTimeAst;
import listener.OnLoadListener;
import model.WeatherTime;

/**
 * Created by lequan on 10/10/2016.
 */
public class WeatherTimeFragment extends Fragment
{
    ListView listView;
    ArrayList<WeatherTime> listWeather;
    WeatherTimeAdt adapter;
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
        WeatherTimeAst asyncTask = new WeatherTimeAst(getActivity().getApplicationContext());
        asyncTask.setListener(new OnLoadListener<ArrayList<WeatherTime>>()
        {
            @Override
            public void onFinish(ArrayList<WeatherTime> weatherTimes)
            {
                listWeather  = weatherTimes;
                adapter = new WeatherTimeAdt(getActivity().getApplicationContext(), R.layout.row_weather_time, listWeather);
                listView.setAdapter(adapter);
                prbLoading.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        });
        asyncTask.execute();

        return convertView;
    }
}
