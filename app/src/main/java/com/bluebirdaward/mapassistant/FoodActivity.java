package com.bluebirdaward.mapassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import adapter.FoodAdt;
import asyncTask.AddressAst;
import asyncTask.FoodAst;
import model.Food;
import model.Restaurant;
import listener.OnLoadListener;
import utils.MapUtils;
import utils.ServiceUtils;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class FoodActivity extends AppCompatActivity implements OnLoadListener<ArrayList<Food>>, OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    GridView gridMenu;
    TextView tvEmpty;
    ArrayList<Food> listFood;
    FoodAdt adapter;
    Restaurant restaurant;
    FloatingActionButton btnGo;
    ProgressBar prbLoading;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        getSupportActionBar().setTitle(restaurant.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnGo = (FloatingActionButton) findViewById(R.id.btnGo);
        gridMenu = (GridView) findViewById(R.id.lvArticle);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);

        listFood = new ArrayList<>();

        adapter = new FoodAdt(getApplicationContext(), R.layout.cell_food, listFood);
        gridMenu.setAdapter(adapter);

        FoodAst asyncTask = new FoodAst();
        asyncTask.setOnLoaded(this);
        asyncTask.execute("https://www.deliverynow.vn" + restaurant.getUrl());

        btnGo.setOnClickListener(this);
    }

    @Override
    public void onFinish(ArrayList<Food> list)
    {
        prbLoading.setVisibility(View.GONE);
        if (list.size() < 1)
        {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < list.size(); ++i)
        {
            listFood.add(list.get(i));
        }
        adapter.notifyDataSetChanged();
        gridMenu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v)
    {
        if (ServiceUtils.checkServiceEnabled(this))
        {
            prbLoading.setVisibility(View.VISIBLE);
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
        else
        {
            Toast.makeText(this, "Bạn chưa mở GPS service", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if (ServiceUtils.checkServiceEnabled(this))
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else
            {
                Toast.makeText(this, getResources().getString(R.string.gps_unabled), Toast.LENGTH_SHORT).show();
            }
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

    synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        buildGoogleApiClient();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(final Location location)
    {
        Log.d("123", ""+location.getLatitude()+"  " + location.getLongitude());

        AddressAst asyncTask = new AddressAst(new Geocoder(this, Locale.getDefault()));
        asyncTask.setListener(new OnLoadListener<String>()
        {
            @Override
            public void onFinish(String myAddress)
            {
                prbLoading.setVisibility(View.GONE);
                Intent intent = new Intent(FoodActivity.this, DirectionActivity.class);
                intent.putExtra("my location", new LatLng(location.getLatitude(), location.getLongitude()));
                intent.putExtra("my address", myAddress);
                intent.putExtra("request", DirectionActivity.RESTAURANT_DIRECTION);
                intent.putExtra("address", MapUtils.minimizeAddress(restaurant.getAddress()));
                intent.putExtra("restaurant", restaurant.getTitle());
                startActivity(intent);
            }
        });
        asyncTask.execute(location.getLatitude(), location.getLongitude());
    }
}
