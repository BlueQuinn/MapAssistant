package com.bluebirdaward.mapassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import Adapter.ViewPagerAdapter;
import Fragment.ContactFragment;
import Fragment.FavouriteFragment;
import Fragment.HistoryFragment;
import Fragment.PlacePickerFragment;
import Listener.OnCloseListener;
import Listener.OnPlaceSelectedListener;
import Utils.RequestCode;

import com.bluebirdaward.mapassistant.gmmap.R;

public class DestinationActivity extends AppCompatActivity
        implements OnPlaceSelectedListener, OnCloseListener
{
    private TabLayout tabLayout;
    PlacePickerFragment fragmentPlace;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            initFragment();
            initContact();

            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            adapter.addFragment(favouriteFragment, "yêu thích");
            adapter.addFragment(historyFragment, "gần đây");
            adapter.addFragment(contactFragment, "danh bạ");
            viewPager.setAdapter(adapter);
            setupViewPager();
        }
        else
        {
            if (android.os.Build.VERSION.SDK_INT > 22)
            {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            }
        }
    }

    ContactFragment contactFragment;
    HistoryFragment historyFragment;
    FavouriteFragment favouriteFragment;

    void setupViewPager()
    {
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fragmentPlace = (PlacePickerFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        fragmentPlace.setOnCloseListener(this);
        fragmentPlace.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)
            {
                LatLng position = place.getLatLng();

                historyFragment.save(place.getName().toString(), place.getAddress().toString());

                Intent intent = new Intent();
                intent.putExtra("place", place.getName().toString());
                intent.putExtra("address", place.getAddress().toString());
                intent.putExtra("position", position);
                setResult(RequestCode.SEARCH_DESTINATION, intent);
                finish();
            }

            @Override
            public void onError(Status status)
            {
            }
        });

        Intent intent = getIntent();
        fragmentPlace.setText(intent.getStringExtra("address"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        initFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(favouriteFragment, "yêu thích");
        adapter.addFragment(historyFragment, "gần đây");

        if (requestCode == 1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                initContact();
                adapter.addFragment(contactFragment, "danh bạ");
            }
        }
        viewPager.setAdapter(adapter);

        setupViewPager();
    }

    void initFragment()
    {
        historyFragment = new HistoryFragment();
        historyFragment.setOnPlaceSelectedListener(this);

        favouriteFragment = new FavouriteFragment();
        favouriteFragment.setOnPlaceSelectedListener(this);
    }

    void initContact()
    {
        contactFragment = new ContactFragment();
        contactFragment.setOnPlaceSelectedListener(this);
    }

    @Override
    public void onSelected(String address)
    {
        fragmentPlace.findPlace(address);
    }

    @Override
    public void onClose()
    {
        finish();
    }
}