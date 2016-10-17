package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import asyncTask.AddTrafficAst;
import asyncTask.AddressAst;
import asyncTask.DirectionAst;
import listener.OnLoadListener;
import model.Place;
import model.Route;
import model.Shortcut;
import model.Traffic;
import model.TrafficCircle;
import model.TrafficLine;
import utils.FirebaseUtils;
import utils.MapUtils;
import utils.TrafficUtils;
import widgets.LoadingDialog;
import widgets.MessageDialog;
import widgets.ShortcutDialog;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static utils.FirebaseUtils.getTrafficCircle;
import static utils.FirebaseUtils.getTrafficLine;

public class DirectionActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnMapClickListener
{
    View root;
    GoogleMap map;
    Polyline polyline;
    int width;
    int height;

    LatLng myLocation;
    //   LatLng destination;
    //   String place, address;

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
    int colorGreen, colorLime, colorRoute, colorAccent;

    int time;
    MarkerOptions waypointOption;
    String jamType;
    int jamId;
    Snackbar snackbar;

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
        colorAccent = getResources().getColor(R.color.colorAccent);

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

        map.setOnMapClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnPolylineClickListener(this);

        geocoder = new Geocoder(this, Locale.getDefault());

        Intent intent = getIntent();
        request = intent.getIntExtra("request", CUSTOM_DIRECTION);
        switch (request)
        {
            case CUSTOM_DIRECTION:
            {
                LatLng position = intent.getParcelableExtra("position");
                float zoom = intent.getFloatExtra("zoom", 15);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
                break;
            }

            case RESTAURANT_DIRECTION:
                try
                {
                    String place = intent.getStringExtra("restaurant");
                    String address = intent.getStringExtra("address");
                    myLocation = intent.getParcelableExtra("my location");
                    String myAddress = intent.getStringExtra("my address");
                    textView[0].setText(myAddress);
                    startOption = new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                    map.addMarker(startOption.title("Bạn đang ở đây").snippet(myAddress));

                    endOption = new MarkerOptions().snippet(place);

                    Intent findPlaceIntent = (new PlaceAutocomplete.IntentBuilder(2)).zzeq(address).zzig(1).build(this);
                    startActivityForResult(findPlaceIntent, 3);
                }
                catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }
                break;

            case PLACE_DIRECTION:
                myLocation = intent.getParcelableExtra("myLocation");
                Place dest = (Place) intent.getSerializableExtra("destination");
                String myAddress = intent.getStringExtra("myAddress");
                //  destination = new LatLng(dest.getLat(), dest.getLng());
                //   String place = dest.getName();
                //    String    address = dest.getAddress();
               /* if (address == null)
                {
                    address = "";
                }*/

                startOption = new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                map.addMarker(startOption.title("Bạn đang ở đây").snippet(myAddress));

                endOption = new MarkerOptions().position(dest.getPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
                map.addMarker(endOption.title(dest.getName()).snippet(dest.getAddress()));

                textView[0].setText(myAddress);
                textView[1].setText(dest.getName());

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
            if (snackbar != null)
            {
                snackbar.dismiss();
            }
            reset();

            String place = data.getStringExtra("place");
            String address = data.getStringExtra("address");
            LatLng pos = data.getParcelableExtra("position");

            textView[requestCode].setText(place);

            BitmapDescriptor icon;
            if (requestCode == 0)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
                startOption = new MarkerOptions().position(pos).icon(icon);
                map.addMarker(startOption.title(place).snippet(address));
                if (endOption != null)
                {
                    map.addMarker(endOption);
                }
            }
            else
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
                endOption = new MarkerOptions().position(pos).icon(icon);
                map.addMarker(endOption.title(place).snippet(address));
                if (startOption != null)
                {
                    map.addMarker(startOption);
                }
            }

           /* if (startOption != null)
            {
                map.addMarker(startOption);
            }
            if (endOption != null)
            {
                map.addMarker(endOption);
            }*/

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16));
            if (isDirected())
            {
                for (Marker i : waypoint)
                {
                    i.remove();
                }

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
                endOption.position(restaurant.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag))
                        .title(restaurant.getName().toString());
                map.addMarker(endOption);


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
                    if (snackbar != null)
                    {
                        snackbar.dismiss();
                    }
                    reset();
                    map.addMarker(startOption).hideInfoWindow();
                    map.addMarker(endOption).hideInfoWindow();

                    navigate(startOption.getPosition(), endOption.getPosition());
                }
                break;

            case R.id.btnAdd:
                if (isDirected())
                {
                    SharedPreferences sharedPref = getSharedPreferences("firstLaunch", MODE_PRIVATE);
                    boolean firstLaunch = sharedPref.getBoolean("addWaypoint", true);
                    if (firstLaunch)
                    {
                        MessageDialog.showMessage(this, colorAccent, R.drawable.traffic, "Thêm điểm dừng", "Nhấn giữ vào các lá cờ màu tím và di chuyển để thêm điểm dừng.\nGiúp bạn dễ dàng tối ưu đường đi của mình hơn");
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("addWaypoint", false);
                        editor.apply();
                    }
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
                    if (isOnline())
                    // prbLoading.setVisibility(View.VISIBLE);
                    {
                        loadTraffic();
                    }
                    else
                    {
                        Toast.makeText(this, "Không có kết nối Internet", Toast.LENGTH_SHORT).show();
                    }
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
        time = TrafficUtils.trafficTime();
        if ((time >= 390 && time <= 720) || (time >= 990 && time <= 1170))
        {
            final LoadingDialog dialog = LoadingDialog.show(this, "Phát hiện những nơi có ùn tắc giao thông trên lộ trình");
            final Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic));
            final Query lineQuery = firebase.child("line").orderByChild("time").startAt(time - 30).endAt(time + 30);
            lineQuery.addListenerForSingleValueEvent(new ValueEventListener()    // chưa load downNode
            {
                @Override
                public void onDataChange(final DataSnapshot lineData)
                {
                    final Query circleQuery = firebase.child("circle").orderByChild("time").startAt(time - 30).endAt(time + 30);
                    circleQuery.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot circleData)
                        {
                            meta = 30;

                            final ArrayList<TrafficCircle> trafficCircles = TrafficUtils.getCircleJam(getTrafficCircle(circleData, meta), route);
                            final ArrayList<TrafficLine> trafficLines = TrafficUtils.getLineJam(getTrafficLine(lineData, meta), route);

                            if (trafficLines.size() > 0 || trafficCircles.size() > 0)
                            {
                                map.clear();
                                if (snackbar != null)
                                {
                                    snackbar.dismiss();
                                }
                                redraw();
                                AddTrafficAst asyncTask = new AddTrafficAst(trafficLines, trafficCircles, map);
                                asyncTask.setListener(new OnLoadListener<Traffic>()
                                {
                                    @Override
                                    public void onFinish(Traffic result)
                                    {
                                        hmTraffic = result;
                                        dialog.dismiss();

                                        SharedPreferences sharedPref = getSharedPreferences("firstLaunch", MODE_PRIVATE);
                                        boolean firstLaunch = sharedPref.getBoolean("detectTraffic", true);
                                        if (firstLaunch)
                                        {
                                            MessageDialog.showMessage(DirectionActivity.this, colorAccent, R.drawable.traffic, "Tránh kẹt xe", "Map Assistant giúp kiểm tra và phát hiện những điểm đang xảy ra ùn tắc giao thông trên lộ trình của bạn.\nTừ nay bạn sẽ không còn phải lo về vấn đề này nữa nhé!");
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putBoolean("detectTraffic", false);
                                            editor.apply();
                                        }

                                        ArrayList<LatLng> pos = new ArrayList<LatLng>();
                                        if (trafficLines.size() > 0)
                                        {
                                            for (TrafficLine line : trafficLines)
                                            {
                                                pos.add(line.getEnd());
                                                pos.add(line.getStart());
                                            }
                                        }
                                        else
                                        {
                                            for (TrafficCircle circle : trafficCircles)
                                            {
                                                pos.add(circle.getCenter());
                                            }
                                        }
                                        if (pos.size() > 0)
                                        {
                                            LatLngBounds bound = MapUtils.getBound(pos.toArray(new LatLng[pos.size()]));
                                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 15));
                                        }
                                    }
                                });
                                asyncTask.execute(meta, getResources().getColor(R.color.yellowLight), getResources().getColor(R.color.redLight));
                            }
                            else
                            {
                                dialog.dismiss("Chưa có điểm kẹt xe nào trên đường đi này");
                            }
                            circleQuery.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError)
                        {
                            // System.out.println("The read failed: " + firebaseError.getMessage());
                            dialog.dismiss();
                            circleQuery.removeEventListener(this);
                        }
                    });
                    lineQuery.removeEventListener(this);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError)
                {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                    prbLoading.setVisibility(View.GONE);
                    lineQuery.removeEventListener(this);
                }
            });
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
        if (marker.getPosition().equals(startOption.getPosition()) || marker.getPosition().equals(endOption.getPosition()))
        {
            return false;
        }
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
                        snackbar = Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE).setActionTextColor(colorLime)
                                .setAction("Xóa", new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        waypoint.remove(m);
                                        marker.remove();
                                    }
                                });
                        snackbar.show();
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
                snackbar = Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE)
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
                                    jamId = line.getId();
                                }
                                else
                                {
                                    TrafficCircle circle = hmTraffic.getCircle(marker.getId());
                                    shortcuts = circle.getShortcuts();
                                    jamType = Traffic.CIRCLE;
                                    jamId = circle.getId();
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
                                    if (snackbar != null)
                                    {
                                        snackbar.dismiss();
                                    }
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
                        });
                snackbar.show();
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
        PolylineOptions options = new PolylineOptions().width(15).color(colorRoute).clickable(true);
        options.addAll(route.getRoute());
        map.addPolyline(options);
    }

    void sendRating(final int shortcutId, final int rating)
    {
        Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic)).child(jamType);
        final Query query = firebase.orderByChild("id").equalTo(jamId);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Firebase shortcutRef = FirebaseUtils.getShortcutRef(dataSnapshot);
                if (shortcutRef != null)
                {
                    final Query shortcutQuery = shortcutRef.orderByChild("id").equalTo(shortcutId);
                    shortcutQuery.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Firebase shortcutNode = FirebaseUtils.getShortcutNode(dataSnapshot);
                            if (shortcutNode != null)
                            {
                                HashMap<String, Object> rateNode = new HashMap<>();
                                rateNode.put("like", rating);
                                shortcutNode.updateChildren(rateNode);
                            }
                            else
                            {
                                MessageDialog.showMessage(DirectionActivity.this, getResources().getColor(R.color.colorPrimary), R.drawable.error, "Đã xảy ra lỗi", "Xin hãy thử lại vào lần sau");
                            }
                            shortcutQuery.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError)
                        {

                        }
                    });
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
                ShortcutDialog dialog = new ShortcutDialog(DirectionActivity.this, shortcut, jamType, jamId);
                dialog.setListener(new OnLoadListener<Integer>()
                {
                    @Override
                    public void onFinish(Integer rating)
                    {
                        if (rating < 0)
                        {
                            map.clear();
                            if (snackbar != null)
                            {
                                snackbar.dismiss();
                            }
                            reset();
                            navigateShortcut(startOption.getPosition(), shortcut.getStart(), shortcut.getEnd(), endOption.getPosition());
                        }
                        else
                        {
                            sendRating(shortcut.getId(), rating);
                            shortcut.setRating(rating);
                        }
                    }
                });
                dialog.show();
            }
        }
        else
        {
            snackbar = Snackbar.make(root, route.getInfo(), Snackbar.LENGTH_INDEFINITE).setActionTextColor(colorLime);
            snackbar.show();
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

    @Override
    public void onMapClick(LatLng latLng)
    {
        if (snackbar != null)
        {
            snackbar.dismiss();
        }
    }

    boolean isOnline()
    {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

