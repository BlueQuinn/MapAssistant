package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import asyncTask.DirectionAst;
import listener.OnLoadListener;
import model.Route;
import model.Traffic;
import model.TrafficOption;
import utils.MapUtils;
import utils.TrafficOptionUtils;
import utils.TrafficUtils;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.ArrayList;

public class DirectionActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener
{
    FloatingActionButton btnTraffic;
    GoogleMap map;
    LatLng position;
    Polyline polyline;
    float zoom;
    int width;
    int height;

    LatLng myLocation;
    LatLng destination;
    String place, address;

    Route route;

    TextView[] textView;

    LatLng[] latLng = new LatLng[2];
    Marker[] marker = new Marker[2];
    ArrayList<Marker> waypoint;

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
        Firebase.setAndroidContext(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        textView = new TextView[2];
        textView[0] = (TextView) findViewById(R.id.txtFrom);
        textView[1] = (TextView) findViewById(R.id.txtTo);

        btnTraffic = (FloatingActionButton) findViewById(R.id.btnTraffic);
        btnTraffic.setVisibility(View.GONE);
        //btnTraffic.setOnClickListener(this);

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
                break;

            case PLACE_DIRECTION:
                myLocation = intent.getParcelableExtra("myLocation");
                destination = intent.getParcelableExtra("destination");
                place = intent.getStringExtra("place");
                address = intent.getStringExtra("address");
                break;
        }

        DisplayMetrics display = getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;
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

        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(DirectionActivity.this);
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
        {
            @Override
            public void onPolylineClick(Polyline polyline)
            {
                Snackbar.make(findViewById(R.id.frameLayout), route.getInformation(), Snackbar.LENGTH_INDEFINITE).show();
            }
        });

        switch (request)
        {
            case CUSTOM_DIRECTION:
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
                break;

            case RESTAURANT_DIRECTION:
                try
                {
                    Intent intent = getIntent();
                    place = intent.getStringExtra("restaurant");
                    address = intent.getStringExtra("address");
                    myLocation = intent.getParcelableExtra("my location");
                    latLng[0] = new LatLng(myLocation.latitude, myLocation.longitude);
                    textView[0].setText("my location");
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng[0]).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                    marker[0] = map.addMarker(markerOptions.title("my location").snippet(intent.getStringExtra("my address")));

                    Intent findPlaceIntent = (new PlaceAutocomplete.IntentBuilder(2)).zzeq(address).zzig(1).build(this);
                    startActivityForResult(findPlaceIntent, 3);
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

            case PLACE_DIRECTION:
                latLng[0] = new LatLng(myLocation.latitude, myLocation.longitude);
                latLng[1] = new LatLng(destination.latitude, destination.longitude);

                MarkerOptions markerOptions = new MarkerOptions().position(latLng[0]).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                marker[0] = map.addMarker(markerOptions.title("my location").draggable(true));

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == 0 || requestCode == 1) && data != null)     // between 2 custom place
        {
            prbLoading.setVisibility(View.GONE);
            String place = data.getStringExtra("place");
            String address = data.getStringExtra("address");
            latLng[requestCode] = data.getParcelableExtra("position");

            textView[requestCode].setText(place);

            BitmapDescriptor icon;
            if (requestCode == 0)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
            }
            else
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
            }

            if (marker[requestCode] != null)
            {
                marker[requestCode].remove();
            }
            MarkerOptions markerOptions = new MarkerOptions().position(latLng[requestCode]).icon(icon);
            marker[requestCode] = map.addMarker(markerOptions.title(place).snippet(address));
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
                prbLoading.setVisibility(View.GONE);
                Place restaurant = PlaceAutocomplete.getPlace(this, data);
                textView[1].setText(restaurant.getName());
                latLng[1] = restaurant.getLatLng();
                MarkerOptions markerOptions = new MarkerOptions().position(latLng[1]).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
                marker[1] = map.addMarker(markerOptions.title(restaurant.getName().toString()));
                if (place.length() > 0)
                {
                    marker[1].setSnippet(place);
                }

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

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(latLng[0], latLng[1]), width, height, 150));

                DirectionActivity.this.route = route;
            }
        });
        asyncTask.execute(point);
        prbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(final View v)
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

            case R.id.btnTraffic:
            {
                prbLoading.setVisibility(View.VISIBLE);
                final Firebase ref = new Firebase(getResources().getString(R.string.database_traffic));
                final ValueEventListener listener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot snapshot)
                    {
                        for (int i = 0; i < route.pathCount(); ++i)
                        {
                            int meta = ((Long) snapshot.child("meta").getValue()).intValue();
                            DataSnapshot rush = snapshot.child("rush");
                            for (DataSnapshot jam : rush.getChildren())
                            {
                                double lat1 = (double) jam.child("lat1").getValue();
                                double lng1 = (double) jam.child("lng1").getValue();
                                double lat2 = (double) jam.child("lat2").getValue();
                                double lng2 = (double) jam.child("lng2").getValue();
                                int vote = ((Long) jam.child("vote").getValue()).intValue();
                                Traffic traffic = (new Traffic(lat1, lng1, lat2, lng2, vote));
                                LatLng intersect = traffic.intersect(traffic.getStart(), traffic.getEnd());
                                if (intersect != null)
                                {
                                    // int vote = ((Long) jam.child("vote").getValue()).intValue();
                                    TrafficOptionUtils optionUtils = new TrafficOptionUtils(meta, getResources().getColor(R.color.yellowLight), getResources().getColor(R.color.redLight));
                                    TrafficOption options = optionUtils.getOption(traffic);
                                    map.addMarker(options.getMarkerOptions());
                                    map.addPolyline(options.getPolylineOptions());
                                }
                            }
                        }
                        ref.removeEventListener(this);
                        prbLoading.setVisibility(View.GONE);

                        waypoint = new ArrayList<>();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError)
                    {
                        ref.removeEventListener(this);
                    }
                };
                ref.addValueEventListener(listener);
            }
            break;
        }
    }

    /*@Override
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
    }*/

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
        navigate(latLng[0], latLng[1], marker.getPosition());
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if (marker.getTitle().equals("Ùn tắc giao thông") || marker.getTitle().equals("Kẹt xe"))
        {
            Snackbar.make(findViewById(R.id.frameLayout), marker.getTitle(), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Tránh", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Marker sign = map.addMarker(new MarkerOptions()
                                    .draggable(true).position(latLng[0])
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sign)));
                            sign.setTitle("Chọn tuyến đường khác");
                            sign.setSnippet("Nhấn giữ để di chuyển");
                            sign.showInfoWindow();

                            waypoint.add(sign);
                        }
                    }).show();
        }
        return false;
    }
}
