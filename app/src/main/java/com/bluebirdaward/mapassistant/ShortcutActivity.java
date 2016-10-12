package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
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
import model.Shortcut;
import model.TrafficCircle;
import model.TrafficLine;
import utils.MapUtils;
import utils.PolyUtils;
import widgets.MessageDialog;

public class ShortcutActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener
{
    FloatingActionButton btnAdd, btnSubmit;
    GoogleMap map;
    ArrayList<Marker> waypoint;
    MarkerOptions option;
    ProgressBar prbLoading;
    Polyline polyline;
    Marker start, end;
    int width, height;
    int ID;
    LatLng jam;
    View root;
    Geocoder geocoder;
    Route route;
    int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        Firebase.setAndroidContext(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đề xuất đường đi tắt");

        root = findViewById(R.id.frameLayout);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        btnAdd = (FloatingActionButton) findViewById(R.id.btnAdd);
        btnSubmit = (FloatingActionButton) findViewById(R.id.btnSubmit);
        btnAdd.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);


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
            LatLng position = new LatLng((start.getPosition().latitude + end.getPosition().latitude) / 2, (start.getPosition().longitude + end.getPosition().longitude) / 2);
            waypoint.add(map.addMarker(option.position(position)));
        }
        else    // submit
        {
            if (!route.inCircle(jam, radius))
            {
                Toast.makeText(this, "Đường đi này đã vượt ra ngoài phạm vi cho phép", Toast.LENGTH_SHORT).show();
                return;
            }

            int acceptedLength = radius + 2000;
            if (route.getDistance() > acceptedLength)
            {
                Toast.makeText(this, "Đường đi tắt phải ngắn hơn " + (acceptedLength / 1000) + "km", Toast.LENGTH_SHORT).show();
                return;
            }

            prbLoading.setVisibility(View.VISIBLE);
            final Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic));
            firebase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    DataSnapshot data = snapshot.child("shortcut");
                    Firebase ref = data.getRef();

                    String polyRoute = PolyUtils.encode(polyline.getPoints());
                    Shortcut shortcut = new Shortcut(jam.latitude, jam.longitude, polyRoute, 1, route.getDistance(), route.getDuration());
                    ref.push().setValue(shortcut);
                    firebase.removeEventListener(this);

                    prbLoading.setVisibility(View.GONE);
                    MainActivity.sqlite.addShortcut(ID, polyRoute, route.getDistance(), route.getDuration());

                    MessageDialog.showMessage(ShortcutActivity.this, R.color.lime, R.drawable.smile, "Đề xuất đường đi thành công", "Cảm ơn bạn đã gợi ý tuyến đường tắt này cho mọi người.\nTất cả người dùng ứng dụng Map Assistant đều sẽ biết được gợi ý của bạn.");
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

        LatLng startPos;
        LatLng endPos;

        Intent intent = getIntent();
        ID = intent.getIntExtra("ID", 0);
        boolean jamType= intent.getBooleanExtra("jamType", true);
        if (jamType)
        {
         TrafficLine line = intent.getParcelableExtra("jam");
            jam = line.getCenter();
            radius = (int) MapUtils.distance(jam, line.getStart());

            startPos  = line.getStart();
            endPos = line.getEnd();
        }
        else
        {
            TrafficCircle circle = intent.getParcelableExtra("jam");
            jam = circle.getCenter();
            radius = circle.getRadius();

            Log.d("shortcut", "" + radius);

            startPos = new LatLng(10.76353877327849, 106.68203115463257);
            endPos = new LatLng(10.411269, 107.136072);
        }

        map = googleMap;
        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnPolylineClickListener(this);

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(startPos, jam, endPos), 10));
        map.addMarker(new MarkerOptions().position(jam).icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_high)));
        map.addCircle(new CircleOptions().center(jam).radius(radius).strokeColor(Color.RED));

        option = new MarkerOptions().draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.sign));
        start = map.addMarker(new MarkerOptions().draggable(true).position(startPos).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).title("Điểm đầu"));
        end = map.addMarker(new MarkerOptions().draggable(true).position(endPos).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)).title("Điểm cuối"));

        waypoint = new ArrayList<>();
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
        ArrayList<LatLng> point = new ArrayList<>();
        point.add(end.getPosition());
        point.add(start.getPosition());
        for (Marker i : waypoint)
        {
            point.add(i.getPosition());
        }
        navigate(point.toArray(new LatLng[point.size()]));
    }

    void navigate(LatLng... point)
    {
        prbLoading.setVisibility(View.VISIBLE);
        DirectionAst asyncTask = new DirectionAst();
        asyncTask.setOnLoadListener(new OnLoadListener<Route>()
        {
            @Override
            public void onFinish(Route route)
            {
                prbLoading.setVisibility(View.GONE);
                if (route == null || route.pathCount() < 1)
                {
                    Toast.makeText(getApplicationContext(), "Không thể tải được dữ liệu", Toast.LENGTH_LONG).show();
                    return;
                }

                if (polyline != null)
                {
                    polyline.remove();
                }
                PolylineOptions option = new PolylineOptions().width(15).color(getResources().getColor(R.color.green));
                option.addAll(route.getRoute());
                polyline = map.addPolyline(option);
                polyline.setClickable(true);

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(start.getPosition(), end.getPosition()), width, height, 10));
                ShortcutActivity.this.route = route;

            }
        });
        asyncTask.execute(point);
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
                if (marker.getId().equals(waypoint.get(0).getId()) || marker.getId().equals(waypoint.get(waypoint.size()-1).getId()))     // must not remove start and end
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

    @Override
    public void onPolylineClick(Polyline polyline)
    {
        Snackbar.make(root, route.getInformation(), Snackbar.LENGTH_INDEFINITE).show();
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
