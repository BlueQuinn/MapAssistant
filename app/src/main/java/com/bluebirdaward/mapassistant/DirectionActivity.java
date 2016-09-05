package com.bluebirdaward.mapassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import AsyncTask.DirectionAst;
import Listener.OnLoadListener;
import Utils.ServiceUtils;

import com.bluebirdaward.mapassistant.gmmap.R;

public class DirectionActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationChangeListener
{
    GoogleMap map;
    LatLng position;
    Polyline route;
    float zoom;
    private int width;
    private int height;

    LatLng myLocation;
    LatLng destination;
    String place, address;

    //String restaurant;

    TextView[] textView;

    LatLng[] latLng = new LatLng[2];
    Marker[] marker = new Marker[2];

    int request;
    final int CUSTOM_DIRECTION = 1;      //
    public static final int RESTAURANT_DIRECTION = 2;    // btnTrack click
    public static final int PLACE_DIRECTION = 3;

    ProgressBar prbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        getSreenDimension();

        textView = new TextView[2];
        textView[0] = (TextView) findViewById(R.id.txtFrom);
        textView[1] = (TextView) findViewById(R.id.txtTo);

        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
        ImageButton btnReverse = (ImageButton) findViewById(R.id.btnReverse);

        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        prbLoading.setVisibility(View.GONE);

        btnBack.setOnClickListener(this);
        btnReverse.setOnClickListener(this);

        textView[0].setOnClickListener(this);
        textView[1].setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        request = intent.getIntExtra("request", CUSTOM_DIRECTION);
        switch (request)
        {
            case CUSTOM_DIRECTION:
                position = intent.getParcelableExtra("position");
                zoom = intent.getFloatExtra("zoom", 15);
                break;

            case RESTAURANT_DIRECTION:
                place = intent.getStringExtra("restaurant");
                address = intent.getStringExtra("address");
                break;

            case PLACE_DIRECTION:
                myLocation = intent.getParcelableExtra("myLocation");
                destination = intent.getParcelableExtra("destination");
                place = intent.getStringExtra("place");
                address = intent.getStringExtra("address");
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setContentDescription(getResources().getString(R.string.app_name));
        map.setTrafficEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        switch (request)
        {
            case CUSTOM_DIRECTION:
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
                break;

            case RESTAURANT_DIRECTION:
            {
                try
                {
                    Intent intent = (new PlaceAutocomplete.IntentBuilder(2)).zzeq(address).zzig(1).build(this);
                    startActivityForResult(intent, 3);
                    //prbLoading.setVisibility(View.VISIBLE);
                }
                catch (GooglePlayServicesRepairableException e)
                {
                    e.printStackTrace();
                }
                catch (GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }
                break;
            }

            case PLACE_DIRECTION:
                latLng[0] = new LatLng(myLocation.latitude, myLocation.longitude);
                latLng[1] = new LatLng(destination.latitude, destination.longitude);

                MarkerOptions markerOptions = new MarkerOptions().position(latLng[0]).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                marker[0] = map.addMarker(markerOptions.title("my location"));
                markerOptions = new MarkerOptions().position(latLng[1]).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
                marker[1] = map.addMarker(markerOptions.title(place));
                if (address != null)
                {
                    marker[1].setSnippet(address);
                }

                textView[0].setText("my location");
                textView[1].setText(place);

                navigate(latLng[0], latLng[1]);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ServiceUtils.checkServiceEnabled(this))
            {
                prbLoading.setVisibility(View.VISIBLE);
                map.setMyLocationEnabled(true);
                map.setOnMyLocationChangeListener(this);
                //Toast.makeText(this, "Đang xác định vị trí của bạn", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Bạn chưa mở GPS service", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == 0 || requestCode == 1) && data != null)     // between 2 custom place
        {
            prbLoading.setVisibility(View.GONE);
            String place = data.getStringExtra("place");
            textView[requestCode].setText(place);

            latLng[requestCode] = data.getParcelableExtra("position");

            BitmapDescriptor icon;
            if (requestCode == 0)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
            }
            else
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
            }
            if (marker[requestCode] == null)
            {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng[requestCode]).icon(icon);
                //Marker = googleMap.addMarker(markerOptions);
                //marker[requestCode] = map.addMarker(new MarkerOptions().position(latLng[requestCode]));
                marker[requestCode] = map.addMarker(markerOptions.title(place));
            }
            else
            {
                marker[requestCode].setPosition(latLng[requestCode]);
            }
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng[requestCode]));

            if (textView[0].getText().length() > 0 && textView[1].getText().length() > 0)
            {
                navigate(latLng[0], latLng[1]);
            }
        }
        if (requestCode == 3)       // direction to restaurant from my location
        {
            if (resultCode == -1)
            {
                Place place = PlaceAutocomplete.getPlace(this, data);
                textView[1].setText(place.getName());
                latLng[1] = place.getLatLng();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    if (android.os.Build.VERSION.SDK_INT > 22)
                    {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    return;
                }
                if (ServiceUtils.checkServiceEnabled(this))
                {
                    prbLoading.setVisibility(View.VISIBLE);
                    map.setMyLocationEnabled(true);
                    map.setOnMyLocationChangeListener(this);
                    //Toast.makeText(this, "Đang xác định vị trí của bạn", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    prbLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Bạn chưa mở GPS service", Toast.LENGTH_SHORT).show();
                }

                /*latLng[0] = new LatLng(myLocation.latitude, myLocation.longitude);
                latLng[1] = place.getLatLng();

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car);
                MarkerOptions markerOptions = new MarkerOptions().position(latLng[0]).icon(icon);
                marker[0] = map.addMarker(markerOptions);

                icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
                markerOptions = new MarkerOptions().position(latLng[1]).icon(icon);
                marker[1] = map.addMarker(markerOptions);

                marker[0].setPosition(latLng[0]);
                marker[1].setPosition(latLng[1]);

                navigate(latLng[0], latLng[1]);*/
            }
            else if (resultCode == 2)
            {
                Status var5 = PlaceAutocomplete.getStatus(this, data);
            }
        }
    }

    void navigate(LatLng start, LatLng end)
    {
        DirectionAst asyncTask = new DirectionAst();
        asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<LatLng>>()
        {
            @Override
            public void onFinish(ArrayList<LatLng> directionPoints)
            {
                prbLoading.setVisibility(View.GONE);
                if (directionPoints == null || directionPoints.size() < 1)
                {
                    Toast.makeText(getApplicationContext(), "Không thể tải được dữ liệu", Toast.LENGTH_LONG).show();
                    return;
                }
                PolylineOptions line = new PolylineOptions().width(15).color(getResources().getColor(R.color.colorPrimary));
                for (int i = 0; i < directionPoints.size(); i++)
                {
                    line.add(directionPoints.get(i));
                }
                if (route != null)
                {
                    route.remove();
                }
                route = map.addPolyline(line);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(createLatLngBoundsObject(latLng[0], latLng[1]), width, height, 150));
            }
        });
        asyncTask.execute(start, end);
        prbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.txtFrom:
                startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", textView[0].getText().toString()), 0);
                break;

            case R.id.txtTo:
                startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", textView[1].getText().toString()), 1);
                break;

            case R.id.btnBack:
                finish();

            case R.id.btnReverse:
                if (textView[0].getText().length() > 0 && textView[1].getText().length() > 0)
                {
                    String tmp = textView[0].getText().toString();
                    textView[0].setText(textView[1].getText());
                    textView[1].setText(tmp);

                    double tmpLat = latLng[0].latitude;
                    double tmpLng = latLng[0].longitude;
                    latLng[0] = new LatLng(latLng[1].latitude, latLng[1].longitude);
                    latLng[1] = new LatLng(tmpLat, tmpLng);

                    tmp = marker[0].getTitle();
                    marker[0].setTitle(marker[1].getTitle());
                    marker[1].setTitle(tmp);

                    Log.d("234", marker[0].getSnippet() + "    " + marker[1].getSnippet());

                    tmp = marker[0].getSnippet();
                    marker[0].setSnippet(marker[1].getSnippet());
                    marker[1].setSnippet(tmp);

                    marker[0].setPosition(latLng[0]);
                    marker[1].setPosition(latLng[1]);

                    marker[0].hideInfoWindow();
                    marker[1].hideInfoWindow();

                    navigate(latLng[0], latLng[1]);
                }
                break;
        }
    }

    private void getSreenDimension()
    {
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    @Override
    public void onMyLocationChange(Location location)   // this callback only run for restaurant direction
    {
        map.setOnMyLocationChangeListener(null);
        prbLoading.setVisibility(View.GONE);
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        latLng[0] = new LatLng(myLocation.latitude, myLocation.longitude);
        textView[0].setText("my location");

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng[0]).icon(icon);
        marker[0] = map.addMarker(markerOptions.title("my location"));

        icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
        markerOptions = new MarkerOptions().position(latLng[1]).icon(icon);
        marker[1] = map.addMarker(markerOptions.title(textView[1].getText().toString()));
        if (place.length() > 0)
        {
            marker[1].setSnippet(place);
        }

        marker[0].setPosition(latLng[0]);
        marker[1].setPosition(latLng[1]);

        navigate(latLng[0], latLng[1]);
    }
}
