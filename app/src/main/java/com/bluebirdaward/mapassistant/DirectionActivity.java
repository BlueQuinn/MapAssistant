package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
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
import Listener.OnDirectionListener;
import com.bluebirdaward.mapassistant.gmmap.R;

public class DirectionActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback, OnDirectionListener
{
    GoogleMap map;
    LatLng position;
    Polyline route;
    float zoom;
    private int width;
    private int height;
    String restaurant;

    ImageButton btnBack;
    ImageButton btnReverse;
    TextView[] textView;

    LatLng[] latLng = new LatLng[2];
    Marker[] marker = new Marker[2];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        getSreenDimanstions();

        textView = new TextView[2];
        textView[0] = (TextView) findViewById(R.id.txtFrom);
        textView[1] = (TextView) findViewById(R.id.txtTo);

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnReverse = (ImageButton) findViewById(R.id.btnReverse);

        setListener();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        position = intent.getParcelableExtra("position");
        zoom = intent.getFloatExtra("zoom", 15);
        restaurant = intent.getStringExtra("restaurant");
    }

        void setListener()
    {
        btnBack.setOnClickListener(this);
        btnReverse.setOnClickListener(this);

        textView[0].setOnClickListener(this);
        textView[1].setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setContentDescription("");
        map.setTrafficEnabled(true);
        if (restaurant == null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
        }
        else
        {
            try
            {
                Intent intent = (new PlaceAutocomplete.IntentBuilder(2)).zzeq(restaurant).zzig(1).build(this);
                startActivityForResult(intent, 3);
            }
            catch (GooglePlayServicesRepairableException e)
            {
                e.printStackTrace();
            }
            catch (GooglePlayServicesNotAvailableException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == 0 || requestCode == 1) && data != null)
        {
            String place = data.getStringExtra("place");
            textView[requestCode].setText(place);

            latLng[requestCode] = data.getParcelableExtra("position");

            BitmapDescriptor icon;
            if (requestCode == 0)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.car);
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
                marker[requestCode] = map.addMarker(markerOptions);
            }
            else
            {
                marker[requestCode].setPosition(latLng[requestCode]);
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng[requestCode], zoom));

            if (textView[0].getText().length() > 0 && textView[1].getText().length() > 0)
            {
                navigate(latLng[0], latLng[1]);
            }
        }
        if (requestCode == 3)
        {
            if (resultCode == -1)
            {
                Place place = PlaceAutocomplete.getPlace(this, data);
                textView[0].setText("my location");
                textView[1].setText(place.getName());
                latLng[0] = MainActivity.myLocation;
                latLng[1] = place.getLatLng();

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car);
                MarkerOptions markerOptions = new MarkerOptions().position(latLng[0]).icon(icon);
                marker[0] = map.addMarker(markerOptions);

                icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
                markerOptions = new MarkerOptions().position(latLng[1]).icon(icon);
                marker[1] = map.addMarker(markerOptions);

                marker[0].setPosition(latLng[0]);
                marker[1].setPosition(latLng[1]);

                navigate(latLng[0], latLng[1]);
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
        asyncTask.execute(start, end);
        asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<LatLng>>()
        {
            @Override
            public void onLoaded(ArrayList<LatLng> directionPoints)
            {
                if (directionPoints == null || directionPoints.size() < 1)
                {
                    Toast.makeText(getApplicationContext(), "Không thể tải được dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }
                PolylineOptions line = new PolylineOptions().width(12).color(getResources().getColor(R.color.colorPrimary));
                for (int i = 0; i < directionPoints.size(); i++)
                {
                    line.add(directionPoints.get(i));
                }
                if (route != null)
                {
                    route.remove();
                }
                route = map.addPolyline(line);
                LatLngBounds latlngBounds = createLatLngBoundsObject(latLng[0], latLng[1]);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
            }
        });
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
                    latLng[0] = new LatLng(latLng[1].latitude,  latLng[1].longitude);
                    latLng[1] = new LatLng(tmpLat,  tmpLng);

                    marker[0].setPosition(latLng[0]);
                    marker[1].setPosition(latLng[1]);
                    navigate(latLng[0], latLng[1]);
                }
                break;
        }
    }

    @Override
    public void onDirection(Place startLocation, Place endLocation)
    {
        final LatLng start = startLocation.getLatLng();
        final LatLng end = endLocation.getLatLng();

        map.addMarker(new MarkerOptions().position(start));
        map.addMarker(new MarkerOptions().position(end));

        DirectionAst asyncTask = new DirectionAst();
        asyncTask.execute(start, end);
        asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<LatLng>>()
        {
            @Override
            public void onLoaded(ArrayList<LatLng> directionPoints)
            {
                PolylineOptions line = new PolylineOptions().width(10).color(getResources().getColor(R.color.colorPrimary));
                for (int i = 0; i < directionPoints.size(); i++)
                {
                    line.add(directionPoints.get(i));
                }
                if (route != null)
                {
                    route.remove();
                }
                route = map.addPolyline(line);
                LatLngBounds latlngBounds = createLatLngBoundsObject(start, end);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
            }
        });
    }

    private void getSreenDimanstions()
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
}
