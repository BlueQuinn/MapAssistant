package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.location.Geocoder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Locale;

import asyncTask.AddressAst;
import asyncTask.DirectionAst;
import listener.OnLoadListener;
import model.Route;
import utils.MapUtils;
import utils.PolyUtils;

public class ShortcutActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener
{
    FloatingActionButton btnAdd, btnSubmit;
    GoogleMap map;
    ArrayList<Marker> waypoint;
    MarkerOptions option;
    ProgressBar prbLoading;
    Polyline polyline;
    LatLng start, end;
    int width, height;
    LatLng jam;
    View root;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        Firebase.setAndroidContext(this);

        root = findViewById(R.id.frameLayout);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        btnAdd = (FloatingActionButton) findViewById(R.id.btnAdd);
        btnSubmit = (FloatingActionButton) findViewById(R.id.btnSubmit);
        btnAdd.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        Intent intent = getIntent();
        //jam = intent.getParcelableExtra("jam");
        jam = new LatLng(10.802355370747835, 106.64164245128632);
        start = new LatLng(10.76353877327849, 106.68203115463257);
        end = new LatLng(10.411269, 107.136072);

        geocoder = new Geocoder(this, Locale.getDefault());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DisplayMetrics display = getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnAdd)
        {
            LatLng position = new LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2);
            waypoint.add(map.addMarker(option.position(position)));
        }
        else
        {
            final Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic));
            firebase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    DataSnapshot shortcut = snapshot.child("shortcut");
                    Firebase ref = shortcut.getRef();

                    String route = PolyUtils.encode(polyline.getPoints());

                    firebase.removeEventListener(this);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError)
                {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                    firebase.removeEventListener(this);
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        prbLoading.setVisibility(View.GONE);

        map = googleMap;
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(start, jam, end), 15));
        map.addMarker(new MarkerOptions().position(jam).icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_high)));
        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(this);

        option = new MarkerOptions().draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sign));

        waypoint = new ArrayList<>();
        map.addMarker(new MarkerOptions().draggable(true).position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        map.addMarker(new MarkerOptions().draggable(true).position(end).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)));

        btnAdd.setClickable(true);
        btnSubmit.setClickable(true);
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {

    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        LatLng[] point = new LatLng[waypoint.size() + 2];
        for (int i = 1; i < point.length - 1; ++i)
        {
            point[i] = waypoint.get(i-1).getPosition();
        }
        point[0] = new LatLng(start.latitude, start.longitude);
        point[point.length - 1] = new LatLng(end.latitude, end.longitude);
        navigate(point);
    }

    void navigate(LatLng... point)
    {
        DirectionAst asyncTask = new DirectionAst();
        asyncTask.setOnLoadListener(new OnLoadListener<Route>()
        {
            @Override
            public void onFinish(Route route)
            {
                prbLoading.setVisibility(View.GONE);
                if (route == null || route.lenght() < 1)
                {
                    Toast.makeText(getApplicationContext(), "Không thể tải được dữ liệu", Toast.LENGTH_LONG).show();
                    return;
                }
                PolylineOptions option = new PolylineOptions().width(15).color(getResources().getColor(R.color.colorPrimary));
                /*for (int i = 0; i < result.lenght(); i++)
                {
                    line.add(result.get(i));
                }*/
                option.addAll(route.getRoute());
                if (polyline != null)
                {
                    polyline.remove();
                }

                polyline = map.addPolyline(option);
                polyline.setClickable(true);

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(start, end), width, height, 150));


            }
        });
        asyncTask.execute(point);
        prbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        prbLoading.setVisibility(View.VISIBLE);
        AddressAst asyncTask = new AddressAst(geocoder);
        asyncTask.setListener(new OnLoadListener<String>()
        {
            @Override
            public void onFinish(String address)
            {
                prbLoading.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE);
                if (marker.getId().equals(waypoint.get(0).getId()) || marker.getId().equals(waypoint.get(waypoint.size()).getId()))     // must not remove start and end
                {
                    snackbar.setAction("Xóa", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            marker.remove();
                        }
                    }).show();
                }
            }
        });
        LatLng position = marker.getPosition();
        asyncTask.execute(position.latitude, position.longitude);
        return false;
    }
}
