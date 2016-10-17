package com.bluebirdaward.mapassistant;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bluebirdaward.mapassistant.gmmap.R;

import adapter.ViewPagerAdapter;
import fragment.WeatherTimeFragment;
import fragment.WeatherDateFragment;
import fragment.WeatherWeekFragment;
import widgets.MessageDialog;

public class WeatherActivity extends AppCompatActivity
{
    ViewPager viewPager;
    ViewPagerAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WeatherDateFragment dateFragment = new WeatherDateFragment();
        WeatherTimeFragment timeFragment = new WeatherTimeFragment();
        WeatherWeekFragment weekFragment = new WeatherWeekFragment();
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(dateFragment,"");
        adapter.addFragment(timeFragment,"");
        adapter.addFragment(weekFragment,"");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.weather_today);
        tabLayout.getTabAt(1).setIcon(R.drawable.clock);
        tabLayout.getTabAt(2).setIcon(R.drawable.calendar);

        SharedPreferences sharedPref = getSharedPreferences("firstLaunch", MODE_PRIVATE);
        boolean firstLaunch = sharedPref.getBoolean("weather", true);
        if (firstLaunch)
        {
            MessageDialog.showMessage(this, getResources().getColor(R.color.colorAccent), R.drawable.weather_today, "Dự báo thời tiết", "Bạn sẽ không còn phải lo lắng về việc ngày hôm nay ra đường gặp những cơn mưa bất chợt nữa.\nVì đã có Map Assistant, giúp cập nhật thông tin thời tiết một cách thường xuyên để tiện cho việc tham gia giao thông");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("weather", false);
            editor.apply();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
