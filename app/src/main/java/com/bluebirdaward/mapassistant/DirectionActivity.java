package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.location.Geocoder;
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
import com.firebase.client.Query;
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

import asyncTask.AddTrafficAst;
import asyncTask.AddressAst;
import asyncTask.DirectionAst;
import listener.OnLoadListener;
import model.Route;
import model.Shortcut;
import model.Traffic;
import model.TrafficCircle;
import model.TrafficLine;
import utils.MapUtils;
import utils.TrafficUtils;
import widgets.ShortcutDialog;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static utils.FirebaseUtils.getTrafficCircle;
import static utils.FirebaseUtils.getTrafficLine;

public class DirectionActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener
{
    View root;
    GoogleMap map;
    Polyline polyline;
    int width;
    int height;

    LatLng myLocation;
    LatLng destination;
    String place, address;

    Route route;
    Geocoder geocoder;

    TextView[] textView;

    LatLng[] latLng = new LatLng[2];
    Marker[] marker = new Marker[2];
    ArrayList<Marker> waypoint;

    Traffic hmTraffic;
    ArrayList<TrafficCircle> trafficCircles;
    ArrayList<TrafficLine> trafficLines;
    HashMap<String, Shortcut> hmShortcut;

    int request;
    final int CUSTOM_DIRECTION = 1;      //
    public static final int RESTAURANT_DIRECTION = 2;    // btnTrack click
    public static final int PLACE_DIRECTION = 3;

    ProgressBar prbLoading;
    int colorGreen, colorLime;

    int time;
    MarkerOptions waypointOption;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        Firebase.setAndroidContext(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        root = findViewById(R.id.frameLayout);
        colorGreen = getResources().getColor(R.color.green);
        colorLime = getResources().getColor(R.color.lime);

        textView = new TextView[2];
        textView[0] = (TextView) findViewById(R.id.txtFrom);
        textView[1] = (TextView) findViewById(R.id.txtTo);

        FloatingActionButton btnTraffic = (FloatingActionButton) findViewById(R.id.btnTraffic);
        FloatingActionButton btnAdd = (FloatingActionButton) findViewById(R.id.btnAdd);
        btnTraffic.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

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
                break;

            case RESTAURANT_DIRECTION:
                break;

            case PLACE_DIRECTION:
                myLocation = intent.getParcelableExtra("myLocation");
                model.Place dest = (model.Place) intent.getSerializableExtra("destination");
                destination = new LatLng(dest.getLat(), dest.getLng());
                place = dest.getName();
                address = dest.getAddress();
                break;
        }

        DisplayMetrics display = getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;

        waypointOption = new MarkerOptions().draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.sign));
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
                Snackbar.make(root, route.getInfo(), Snackbar.LENGTH_INDEFINITE).show();
            }
        });

        geocoder = new Geocoder(this, Locale.getDefault());

        switch (request)
        {
            case CUSTOM_DIRECTION:
            {
                Intent intent = getIntent();
                LatLng position = intent.getParcelableExtra("position");
                float zoom = intent.getFloatExtra("zoom", 15);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
                break;
            }

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

                waypoint = new ArrayList<>();
                navigate(latLng[0], latLng[1]);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        waypoint = new ArrayList<>();

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
            public void onFinish(final Route route)
            {
                prbLoading.setVisibility(View.GONE);
                if (route == null || route.pathCount() < 1)
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
                map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
                {
                    @Override
                    public void onPolylineClick(Polyline polyline)
                    {
                        Snackbar.make(root, route.getInfo(), Snackbar.LENGTH_INDEFINITE).setActionTextColor(colorLime).show();
                    }
                });
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

                    //MarkerOptions options = new MarkerOptions().

                    for (Marker m : waypoint)
                    {
                        m.remove();
                    }
                    navigate(latLng[0], latLng[1]);
                }
                break;

            case R.id.btnAdd:
                if (textView[0].getText().length() > 0 && textView[1].getText().length() > 0)
                {
                    LatLng position = new LatLng((latLng[0].latitude + latLng[1].latitude) / 2, (latLng[0].longitude + latLng[1].longitude) / 2);
                    waypoint.add(map.addMarker(waypointOption.position(position)));
                }
                else
                {
                    Toast.makeText(this, "Hãy chọn địa điểm trước", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnTraffic:
                if (textView[0].getText().length() > 0 && textView[1].getText().length() > 0)
                {
                    prbLoading.setVisibility(View.VISIBLE);
                    loadTraffic();
                }
                else
                {
                    Toast.makeText(this, "Hãy chọn địa điểm trước", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    void loadTraffic()
    {
        time = Integer.parseInt(TrafficUtils.getTimeNode());
        if ((time >= 390 && time <= 720) || (time >= 990 && time <= 1170))
        {
            prbLoading.setVisibility(View.VISIBLE);
            final Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic)).child(Integer.toString(time));
            final ValueEventListener listener = new ValueEventListener()    // chưa load downNode
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    prbLoading.setVisibility(View.VISIBLE);
                    int meta = ((Long) snapshot.child("meta").getValue()).intValue();

                    trafficCircles = TrafficUtils.getCircleJam(getTrafficCircle(snapshot, meta), route);
                    trafficLines = TrafficUtils.getLineJam(getTrafficLine(snapshot, meta), route);

                    if (trafficLines.size() > 0 || trafficCircles.size() > 0)
                    {
                        //map.clear();
                        AddTrafficAst asyncTask = new AddTrafficAst(trafficLines, trafficCircles, map);
                        asyncTask.setListener(new OnLoadListener<Traffic>()
                        {
                            @Override
                            public void onFinish(Traffic result)
                            {
                                hmTraffic = result;
                                getShorcut();
                                prbLoading.setVisibility(View.GONE);
                            }
                        });
                        asyncTask.execute(meta, getResources().getColor(R.color.yellowLight), getResources().getColor(R.color.redLight));
                    }
                    else
                    {
                        prbLoading.setVisibility(View.GONE);
                        Toast.makeText(DirectionActivity.this, "Chưa có nơi nào tắc đường", Toast.LENGTH_SHORT).show();
                    }
                    firebase.removeEventListener(this);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError)
                {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                    prbLoading.setVisibility(View.GONE);
                    firebase.removeEventListener(this);
                }
            };
            firebase.addValueEventListener(listener);
        }
        else
        {
            Toast.makeText(this, "Hiện chưa có điểm kẹt xe nào", Toast.LENGTH_SHORT).show();
        }
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
        point.add(latLng[0]);
        point.add(latLng[1]);
        for (Marker i : waypoint)
        {
            point.add(i.getPosition());
        }
        navigate(point.toArray(new LatLng[point.size()]));
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

    void getShorcut()
    {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                AddressAst asyncTask = new AddressAst(geocoder);
                asyncTask.setListener(new OnLoadListener<String>()
                {
                    @Override
                    public void onFinish(String address)
                    {
                        prbLoading.setVisibility(View.GONE);
                        Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Gợi ý đường tắt", new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        prbLoading.setVisibility(View.VISIBLE);

                                        hmShortcut = new HashMap<>();
                                        ArrayList<Shortcut> shortcuts;
                                        final String jamType;
                                        final int ID;
                                        if (hmTraffic.isLine(marker.getId()))
                                        {
                                            TrafficLine line = hmTraffic.getLine(marker.getId());
                                            shortcuts = line.getShortcuts();
                                            jamType = Traffic.LINE;
                                            ID = line.getId();
                                        }
                                        else
                                        {
                                            TrafficCircle circle = hmTraffic.getCircle(marker.getId());
                                            shortcuts = circle.getShortcuts();
                                            jamType = Traffic.CIRCLE;
                                            ID = circle.getId();
                                        }

                                        if (shortcuts.size() > 0)
                                        {
                                            for (Shortcut i : shortcuts)
                                            {
                                                PolylineOptions options = new PolylineOptions().color(colorGreen).width(15);
                                                options.addAll(i.getRoute());
                                                Polyline polyline = map.addPolyline(options);
                                                hmShortcut.put(polyline.getId(), i);

                                                addStart(i.getStart());
                                                addEnd(i.getEnd());
                                            }
                                            prbLoading.setVisibility(View.GONE);

                                            map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
                                            {
                                                @Override
                                                public void onPolylineClick(Polyline polyline)
                                                {
                                                    final Shortcut shortcut = hmShortcut.get(polyline.getId());
                                                    ShortcutDialog dialog = new ShortcutDialog(DirectionActivity.this, shortcut, time, jamType, ID);
                                                    dialog.setListener(new OnLoadListener<Integer>()
                                                    {
                                                        @Override
                                                        public void onFinish(Integer rating)
                                                        {
                                                            if (rating < 0)
                                                            {
                                                                waypoint = new ArrayList<>();
                                                                navigate(latLng[0], shortcut.getStart(), shortcut.getEnd(), latLng[1]);
                                                            }
                                                            else
                                                            {
                                                                rating(jamType, ID, rating, shortcut.getRouteString());
                                                            }
                                                        }
                                                    });
                                                    dialog.show();
                                                }
                                            });
                                        }
                                        else
                                        {
                                            prbLoading.setVisibility(View.GONE);
                                            Toast.makeText(DirectionActivity.this, "Hiện chưa có tuyến đường tắt nào", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).show();
                    }

                    ;
                });
                asyncTask.execute(marker.getPosition().latitude, marker.getPosition().longitude);
                return false;
            }
        });
    }

    void addStart(LatLng start)
    {
        MarkerOptions options = new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        map.addMarker(options);
    }

    void addEnd(LatLng end)
    {
        MarkerOptions options = new MarkerOptions().position(end).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
        map.addMarker(options);
    }

    void redraw()
    {
    }

    void rating(String jamType, int ID, final int rating, final String route)
    {
        Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic)).child(Integer.toString(time) + "/" + jamType);
        final Query query = firebase.orderByChild("id").equalTo(ID);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                DataSnapshot data = dataSnapshot.child("shortcut");
                for (DataSnapshot shortcut : data.getChildren())
                {
                    if (route.equals(shortcut.child("route").getValue()))
                    {
                        Firebase ref = shortcut.getRef();
                        Map<String, Object> rateNode = new HashMap<>();
                        rateNode.put("rate", rating);
                        ref.updateChildren(rateNode);
                    }
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
                query.removeEventListener(this);
            }
        });
    }

}
