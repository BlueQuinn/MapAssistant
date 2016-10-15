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
import widgets.LoadingDialog;
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
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnPolylineClickListener
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
    int meta;
    MarkerOptions startOption, endOption;
    ArrayList<Marker> waypoint;

    Traffic hmTraffic;
    HashMap<String, Shortcut> hmShortcut;

    int request;
    final int CUSTOM_DIRECTION = 1;      //
    public static final int RESTAURANT_DIRECTION = 2;    // btnTrack click
    public static final int PLACE_DIRECTION = 3;

    ProgressBar prbLoading;
    int colorGreen, colorLime, colorRoute;

    int time;
    MarkerOptions waypointOption;
    String jamType;
    int ID;


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
        colorRoute = getResources().getColor(R.color.routeColor);

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
                if (address == null)
                {
                    address = "";
                }
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
        map.setOnMarkerClickListener(this);
        map.setOnPolylineClickListener(this);

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
                    myLocation = intent.getParcelableExtra("Bạn đang ở đây");
                    // start = new Place(myLocation.latitude, myLocation.longitude, "Bạn đang ở đây", intent.getStringExtra("my address"));
                    textView[0].setText("Bạn đang ở đây");
                    startOption = new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2));
                    map.addMarker(startOption.title("Bạn đang ở đây").snippet(intent.getStringExtra("my address")));

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

                startOption = new MarkerOptions().position(new LatLng(myLocation.latitude, myLocation.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                map.addMarker(startOption.title("Bạn đang ở đây").snippet(""));//.draggable(true));

                endOption = new MarkerOptions().position(new LatLng(destination.latitude, destination.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
                map.addMarker(endOption.title(place).snippet(address));

                textView[0].setText("Bạn đang ở đây");
                textView[1].setText(place);

                waypoint = new ArrayList<>();
                navigate(startOption.getPosition(), endOption.getPosition());
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

            map.clear();

            String place = data.getStringExtra("place");
            String address = data.getStringExtra("address");
            LatLng pos = data.getParcelableExtra("position");

            textView[requestCode].setText(place);

            BitmapDescriptor icon;
            if (requestCode == 0)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.marker2);
            }
            else
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.flag2);
            }

            /*if (marker[requestCode] != null)
            {
                marker[requestCode].remove();
            }*/
            if (requestCode == 0)
            {
                startOption = new MarkerOptions().position(pos).icon(icon);
                map.addMarker(startOption.title(place).snippet(address));
            }
            else
            {
                endOption = new MarkerOptions().position(pos).icon(icon);
                map.addMarker(endOption.title(place).snippet(address));
            }

            map.clear();
            reset();
            if (startOption != null)
            {
                map.addMarker(startOption);
            }
            if (endOption != null)
            {
                map.addMarker(endOption);
            }

            map.animateCamera(CameraUpdateFactory.newLatLng(pos));
            if (isDirected())
            {
                for (Marker i : waypoint)
                {
                    i.remove();
                }

               /* ArrayList<String> key = new ArrayList<>(hmShortcut.keySet());
                for (String i : key)
                hmShortcut.get(i).*/

                navigate(startOption.getPosition(), endOption.getPosition());
            }
        }
        if (requestCode == 3)       // direction to restaurant from my location
        {
            if (resultCode == -1)
            {
                prbLoading.setVisibility(View.GONE);
                com.google.android.gms.location.places.Place restaurant = PlaceAutocomplete.getPlace(this, data);
                textView[1].setText(restaurant.getName());
                endOption = new MarkerOptions().position(restaurant.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
                if (place.length() < 1)
                {
                    place = "";
                }
                map.addMarker(endOption.title(restaurant.getName().toString()).snippet(place));


                navigate(startOption.getPosition(), endOption.getPosition());
            }
            else if (resultCode == 2)
            {
                Status var5 = PlaceAutocomplete.getStatus(this, data);
            }
        }
    }

    void navigate(LatLng... point)
    {
        //hmShortcut = null;

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
                PolylineOptions option = new PolylineOptions().width(15).color(getResources().getColor(R.color.routeColor));
                /*for (int i = 0; i < result.lenght(); i++)
                {
                    line.add(result.get(i));
                }*/
                option.addAll(route.getRoute()).clickable(true);
                if (polyline != null)
                {
                    polyline.remove();
                }

                polyline = map.addPolyline(option);
                polyline.setClickable(true);

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(startOption.getPosition(), endOption.getPosition()), width, height, 150));

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

                    double tmpLat = startOption.getPosition().latitude;
                    double tmpLng = startOption.getPosition().longitude;
                    startOption.position(new LatLng(endOption.getPosition().latitude, endOption.getPosition().longitude));
                    endOption.position(new LatLng(tmpLat, tmpLng));

                    tmp = startOption.getTitle();
                    startOption.title(endOption.getTitle());
                    endOption.title(tmp);

                    tmp = startOption.getSnippet();
                    startOption.snippet(endOption.getSnippet());
                    endOption.snippet(tmp);

                    map.clear();
                    reset();
                    map.addMarker(startOption).hideInfoWindow();
                    map.addMarker(endOption).hideInfoWindow();

                    navigate(startOption.getPosition(), endOption.getPosition());
                }
                break;

            case R.id.btnAdd:
                if (isDirected())
                {
                    LatLng position = new LatLng((startOption.getPosition().latitude + endOption.getPosition().latitude) / 2, (startOption.getPosition().longitude + endOption.getPosition().longitude) / 2);
                    waypoint.add(map.addMarker(waypointOption.position(position)));
                }
                else
                {
                    Toast.makeText(this, "Hãy chọn địa điểm trước", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnTraffic:
                if (isDirected())
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
            final LoadingDialog dialog = LoadingDialog.show(this, "Phát hiện những điểm ùn tắc giao thông gần đấy");
            final Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic)).child(Integer.toString(time));
            firebase.addValueEventListener(new ValueEventListener()    // chưa load downNode
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    dialog.dismiss();
                    meta = ((Long) snapshot.child("meta").getValue()).intValue();

                    ArrayList<TrafficCircle> trafficCircles = TrafficUtils.getCircleJam(getTrafficCircle(snapshot, meta), route);
                    ArrayList<TrafficLine> trafficLines = TrafficUtils.getLineJam(getTrafficLine(snapshot, meta), route);

                    if (trafficLines.size() > 0 || trafficCircles.size() > 0)
                    {
                        redraw();
                        AddTrafficAst asyncTask = new AddTrafficAst(trafficLines, trafficCircles, map);
                        asyncTask.setListener(new OnLoadListener<Traffic>()
                        {
                            @Override
                            public void onFinish(Traffic result)
                            {

                                hmTraffic = result;
                                prbLoading.setVisibility(View.GONE);
                            }
                        });
                        asyncTask.execute(meta, getResources().getColor(R.color.yellowLight), getResources().getColor(R.color.redLight));
                    }
                    else
                    {
                        /*prbLoading.setVisibility(View.GONE);
                        Toast.makeText(DirectionActivity.this, "Chưa có điểm kẹt xe nào trên đường đi này", Toast.LENGTH_SHORT).show();*/
                        dialog.dismiss("Chưa có điểm kẹt xe nào trên đường đi này");
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
            });
        }
        else
        {
            prbLoading.setVisibility(View.VISIBLE);
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
        point.add(startOption.getPosition());
        point.add(endOption.getPosition());
        for (Marker i : waypoint)
        {
            point.add(i.getPosition());
        }
        navigate(point.toArray(new LatLng[point.size()]));
    }

    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        for (final Marker m : waypoint)
        {
            if (m.getId().equals(marker.getId()))
            {
                AddressAst asyncTask = new AddressAst(geocoder);
                asyncTask.setListener(new OnLoadListener<String>()
                {
                    @Override
                    public void onFinish(String address)
                    {
                        Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE).setActionTextColor(colorLime)
                                .setAction("Xóa", new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        waypoint.remove(m);
                                        marker.remove();
                                    }
                                }).show();
                    }
                });
                asyncTask.execute(marker.getPosition().latitude, marker.getPosition().longitude);
                return false;
            }
        }

        AddressAst asyncTask = new AddressAst(geocoder);
        asyncTask.setListener(new OnLoadListener<String>()
        {
            @Override
            public void onFinish(String address)
            {
                prbLoading.setVisibility(View.GONE);
                Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Đường tắt", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                prbLoading.setVisibility(View.VISIBLE);

                                hmShortcut = new HashMap<>();
                                ArrayList<Shortcut> shortcuts;
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

                                if (shortcuts != null && shortcuts.size() > 0)
                                {
                                    MarkerOptions markerOptions = new MarkerOptions().position(marker.getPosition())
                                            .title(marker.getTitle()).snippet(marker.getSnippet());
                                    if (getRating(marker.getSnippet()) > 2 * meta)
                                    {
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_high));
                                    }
                                    else
                                    {
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_medium));
                                    }
                                    map.clear();
                                    map.addMarker(markerOptions);
                                    redraw();
                                    for (int i = 0; i < shortcuts.size(); ++i)
                                    {
                                        PolylineOptions options = new PolylineOptions().color(colorGreen).width(15);
                                        options.addAll(shortcuts.get(i).getRoute()).clickable(true).zIndex(10);
                                        Polyline polyline = map.addPolyline(options);
                                        hmShortcut.put(polyline.getId(), shortcuts.get(i));

                                        addStart(shortcuts.get(i).getStart(), i);
                                        addEnd(shortcuts.get(i).getEnd(), i);
                                    }
                                    prbLoading.setVisibility(View.GONE);
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));
                                }
                                else
                                {
                                    prbLoading.setVisibility(View.GONE);
                                    Toast.makeText(DirectionActivity.this, "Hiện chưa có tuyến đường tắt nào", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
            }
        });
        asyncTask.execute(marker.getPosition().latitude, marker.getPosition().longitude);
        return false;

    }


    void addStart(LatLng start, int i)
    {
        MarkerOptions options = new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_shortcut));
        map.addMarker(options.title("Tuyến đường tắt số " + (i + 1)).snippet("Điểm đầu")).showInfoWindow();
    }

    void addEnd(LatLng end, int i)
    {
        MarkerOptions options = new MarkerOptions().position(end).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_shortcut));
        map.addMarker(options.title("Tuyến đường tắt số " + (i + 1)).snippet("Điểm cuối"));
    }

    void redraw()
    {
        map.addMarker(startOption);
        map.addMarker(endOption);
        PolylineOptions options = new PolylineOptions().width(15).color(colorRoute);
        options.addAll(route.getRoute());
        map.addPolyline(options);
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

    Shortcut shortcut;

    @Override
    public void onPolylineClick(Polyline polyline)
    {
        if (hmShortcut != null)
        {
            shortcut = hmShortcut.get(polyline.getId());
            if (shortcut != null)
            {
                ShortcutDialog dialog = new ShortcutDialog(DirectionActivity.this, shortcut, time, jamType, ID);
                dialog.setListener(new OnLoadListener<Integer>()
                {
                    @Override
                    public void onFinish(Integer rating)
                    {
                        if (rating < 0)
                        {
                            map.clear();
                            reset();
                            navigateShortcut(startOption.getPosition(), shortcut.getStart(), shortcut.getEnd(), endOption.getPosition());
                        }
                        else
                        {
                            rating(jamType, ID, rating, shortcut.getRouteString());
                        }
                    }
                });
                dialog.show();
            }
        }
        else
        {
            Snackbar.make(root, route.getInfo(), Snackbar.LENGTH_INDEFINITE).setActionTextColor(colorLime).show();
        }
    }

    boolean isDirected()
    {
        return textView[0].getText().length() > 0 && textView[1].getText().length() > 0;
    }

    void reset()
    {
        hmShortcut = null;
        hmTraffic = null;
        waypoint = new ArrayList<>();
    }

    void navigateShortcut(final LatLng... point)
    {
        DirectionAst asyncTask = new DirectionAst();
        asyncTask.setOnLoadListener(new OnLoadListener<Route>()
        {
            @Override
            public void onFinish(final Route route)
            {
                if (route == null || route.pathCount() < 1)
                {
                    Toast.makeText(getApplicationContext(), "Không thể tải được dữ liệu", Toast.LENGTH_LONG).show();
                    return;
                }

                final Route firstRoute = route;

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

                        /*if (polyline != null)
                        {
                            polyline.remove();
                        }*/

                        PolylineOptions option = new PolylineOptions().width(15).color(colorRoute).clickable(true);
                        option.addAll(firstRoute.getRoute());
                        polyline = map.addPolyline(option);

                        option = new PolylineOptions().width(15).color(colorGreen).clickable(true);
                        option.addAll(shortcut.getRoute());
                        polyline = map.addPolyline(option);

                        option = new PolylineOptions().width(15).color(colorRoute).clickable(true);
                        option.addAll(route.getRoute());
                        polyline = map.addPolyline(option);

                        map.addMarker(waypointOption.position(shortcut.getStart()));
                        map.addMarker(waypointOption.position(shortcut.getEnd()));
                        map.addMarker(startOption);
                        map.addMarker(endOption);
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(startOption.getPosition(), endOption.getPosition()), width, height, 150));

                        DirectionActivity.this.route = new Route(firstRoute.getPath());
                        DirectionActivity.this.route.add(route.getPath());
                    }
                });
                asyncTask.execute(point[2], point[3]);
                prbLoading.setVisibility(View.VISIBLE);
            }
        });
        asyncTask.execute(point[0], point[1]);
        prbLoading.setVisibility(View.VISIBLE);
    }

    int getRating(String markerSnippet)
    {
        String[] title = markerSnippet.split(" ");
        if (title[0].length() == 1)
        {
            return Integer.parseInt(title[0].substring(0, 1));
        }
        else
        {
            return Integer.parseInt(title[0].substring(0, 1)) * 10 + Integer.parseInt(title[0].substring(1, 2));
        }
    }
}

