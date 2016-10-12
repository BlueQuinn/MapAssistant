package com.bluebirdaward.mapassistant;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import adapter.MenuAdt;
import asyncTask.AddTrafficAst;
import asyncTask.AddressAst;
import asyncTask.FindPlaceAst;
import model.MenuSection;
import model.Menu;
import model.MyTraffic;
import model.Nearby;
import model.Place;
import model.Shortcut;
import model.TrafficLine;
import listener.OnLoadListener;
import model.TrafficCircle;
import model.TrafficType;
import sqlite.SqliteHelper;

import static utils.FirebaseUtils.*;

import utils.MapUtils;
import utils.ServiceUtils;
import utils.TimeUtils;
import widgets.PlacePickerDialog;

import static utils.RequestCode.*;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, View.OnLongClickListener,
        ExpandableListView.OnChildClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener

{
    Place destination;

    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    ProgressBar prbLoading;
    LatLng myLocation;//, destination;

    View root;
    TextView txtSearch;
    //String place = "", address = "";
    public static SqliteHelper sqlite;
    Geocoder geocoder;
    ExpandableListView lvLeftmenu;
    DrawerLayout drawerLayout;

    int request = -1;

    HashMap<String, TrafficType> hmMarker;
    ArrayList<TrafficCircle> trafficCircles;
    ArrayList<TrafficLine> trafficLine;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        root = findViewById(R.id.frameLayout);
        FloatingActionButton btnTrack = (FloatingActionButton) findViewById(R.id.btnTrack);
        FloatingActionButton btnFavourite = (FloatingActionButton) findViewById(R.id.btnDirection);
        ImageButton btnMenu = (ImageButton) findViewById(R.id.btnMenu);
        ImageButton btnVoice = (ImageButton) findViewById(R.id.btnVoice);
        txtSearch = (TextView) findViewById(R.id.txtSearch);
        lvLeftmenu = (ExpandableListView) findViewById(R.id.lvLeftMenu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);

        btnTrack.setOnClickListener(this);
        btnFavourite.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        txtSearch.setOnClickListener(this);
        prbLoading.setVisibility(View.GONE);

        geocoder = new Geocoder(this, Locale.getDefault());

        initMenu();
        initDatabase();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();
    }

    void initMenu()
    {
        ArrayList<Menu> listMenu = new ArrayList<>();
        ArrayList<MenuSection> listSection = new ArrayList<>();

        listMenu.add(new Menu("Tìm nhà hàng", R.drawable.restaurant));
        listMenu.add(new Menu("Tìm địa điểm", R.drawable.place));
        listMenu.add(new Menu("Thời tiết", R.drawable.weather));
        listMenu.add(new Menu("Giá xăng dầu", R.drawable.petrol));
        listMenu.add(new Menu("Chia sẻ", R.drawable.share));
        listSection.add(new MenuSection("Tiện ích", listMenu));

        listMenu = new ArrayList<>();
        listMenu.add(new Menu("Thông báo tắc đường", R.drawable.warning));
        listMenu.add(new Menu("Tình trạng giao thông", R.drawable.traffic_cone));
        //listMenu.add(new Menu("Tình trạng giao thông", R.drawable.traffic_cone));
        listSection.add(new MenuSection("Giao thông", listMenu));

        listMenu = new ArrayList<>();
        listMenu.add(new Menu("Bản đồ thường", R.drawable.normal));
        listMenu.add(new Menu("Xem từ vệ tinh", R.drawable.satellite));
        listMenu.add(new Menu("Xem theo địa hình", R.drawable.terrain));
        listSection.add(new MenuSection("Xem bản đồ", listMenu));

        MenuAdt adapter = new MenuAdt(getApplicationContext(), R.layout.row_menu, R.layout.row_section, listSection);
        lvLeftmenu.setAdapter(adapter);
        lvLeftmenu.setOnChildClickListener(this);
    }

    void initDatabase()
    {
        sqlite = new SqliteHelper(getApplicationContext(), "Destination.sqlite");
        try
        {
            sqlite.createDataBase();
            sqlite.openDataBase();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleApiClient.connect();

        map = googleMap;
        map.setContentDescription(getResources().getString(R.string.app_name));
        map.setTrafficEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        SharedPreferences sharedPref = getSharedPreferences("myLocation", MODE_PRIVATE);
        String myLat = sharedPref.getString("myLat", "");
        String myLng = sharedPref.getString("myLng", "");
        if (myLat.length() < 1 || myLng.length() < 1)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(10.78261522192309, 106.69588862681348), 20));
        }
        else
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(myLat), Double.parseDouble(myLng)), 19));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(MainActivity.this, "Bạn chưa bật GPS Location", Toast.LENGTH_SHORT).show();
            requestLocation(LOCATE_ON_START);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (data != null)
        {
            map.clear();
            switch (requestCode)
            {
                case SEARCH_DESTINATION:
                {
                    String place = data.getStringExtra("place");
                    String address = data.getStringExtra("address");
                    txtSearch.setText(place);

                    LatLng position = data.getParcelableExtra("position");
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16));

                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
                    map.addMarker(new MarkerOptions().icon(icon).position(position).title(place).snippet(address));
                    setFavouriteClick();
                    break;
                }

                case VOICE_SEARCH:
                {
                    if (resultCode == RESULT_OK)
                    {
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        txtSearch.setText(result.get(0));
                        startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", txtSearch.getText().toString()), SEARCH_DESTINATION);
                    }
                    break;
                }

                case LOCATE_TO_NOTIFY:
                {
                    ArrayList<MyTraffic> traffic = sqlite.getMyTraffic();
                    if (traffic.size() > 0)
                    {
                        map.clear();

                        hmMarker = new HashMap<>();
                        trafficCircles = new ArrayList<>();
                        trafficLine = null;

                        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_high));
                        LatLng[] points = new LatLng[traffic.size()];
                        int j = 0;
                        for (MyTraffic i : traffic)
                        {
                            points[j] = i.getLocation();

                            markerOptions.position(i.getLocation()).title(i.getTime()).snippet(i.getAddress());
                            Marker marker = map.addMarker(markerOptions);
                            hmMarker.put(marker.getId(), new TrafficType(false, j));
                            trafficCircles.add(new TrafficCircle(i.getLocation(), i.getRadius(), i.getId()));

                            ArrayList<Shortcut> shortcuts = sqlite.getShortcut(i.getId(), i.getLocation());
                            for (Shortcut s : shortcuts)
                            {
                                ArrayList<LatLng> route = s.getPolyRoute();
                                map.addMarker(new MarkerOptions().position(route.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                map.addMarker(new MarkerOptions().position(route.get(route.size() -1 )).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)));

                                PolylineOptions polylineOptions = new PolylineOptions().width(15).color(getResources().getColor(R.color.green));
                                polylineOptions.addAll(route);
                                map.addPolyline(polylineOptions);
                            }
                            j++;
                        }
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(MapUtils.getBound(points), 15));
                        setTrafficClick(false);
                    }
                    else
                    {
                        Toast.makeText(this, "Bạn chưa thông báo điểm tắc đường nào", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }


            }
        }
    }

    void requestLocation(int requestCode)
    {
        if (android.os.Build.VERSION.SDK_INT > 22)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if (ServiceUtils.checkServiceEnabled(this))
            {
                prbLoading.setVisibility(View.VISIBLE);
                map.setMyLocationEnabled(true);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                //Toast.makeText(this, getResources().getString(R.string.gps_loading), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, getResources().getString(R.string.gps_unabled), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnTrack:
            {
                openGPS(LOCATE_ON_REQUEST);
                break;
            }

            case R.id.txtSearch:
                startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", txtSearch.getText().toString()), SEARCH_DESTINATION);
                break;

            case R.id.btnDirection:
            {
                Intent intent = new Intent(MainActivity.this, DirectionActivity.class);
                if (destination != null)
                {
                    intent.putExtra("request", DirectionActivity.PLACE_DIRECTION);
                    intent.putExtra("myLocation", myLocation);
                    intent.putExtra("destination", destination);
                }
                else
                {
                    if (myLocation == null)
                    {
                        intent.putExtra("position", new LatLng(10.78261522192309, 106.69588862681348));
                    }
                    else
                    {
                        intent.putExtra("position", myLocation);
                    }
                    intent.putExtra("zoom", map.getCameraPosition().zoom);
                }
                startActivity(intent);
                break;
            }

            case R.id.btnVoice:
            {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bạn cần tìm gì ?");
                startActivityForResult(intent, VOICE_SEARCH);
                break;
            }

            case R.id.btnMenu:
                drawerLayout.openDrawer(lvLeftmenu);
                break;
        }
    }

    void showDialog()
    {
        final PlacePickerDialog dialog = new PlacePickerDialog(this);
        dialog.setOnPickListener(new OnLoadListener<Nearby>()
        {
            @Override
            public void onFinish(Nearby nearby)
            {
                final int radius = nearby.getRadius();
                FindPlaceAst asyncTask = new FindPlaceAst(getResources().getString(R.string.google_maps_key), nearby.getPlaceType(), radius);
                asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<Place>>()
                {
                    @Override
                    public void onFinish(ArrayList<Place> list)
                    {
                        if (list == null || list.size() < 1)
                        {
                            Toast.makeText(getApplicationContext(), "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        map.clear();
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
                        MarkerOptions options = new MarkerOptions().icon(icon);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (int i = 0; i < list.size(); ++i)
                        {
                            Place place = list.get(i);
                            LatLng position = new LatLng(place.getLat(), place.getLng());
                            map.addMarker(options.position(position).title(place.getName()).snippet(place.getAddress()));
                            builder.include(position);
                        }
                        map.addCircle(new CircleOptions().center(myLocation).radius(1000 * radius).strokeColor(Color.RED));
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                        setFavouriteClick();
                    }
                });
                asyncTask.execute(myLocation.latitude, myLocation.longitude);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onLongClick(View v)
    {
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
        switch (groupPosition)
        {
            case 0:
            {
                switch (childPosition)
                {
                    case 0:     // restaurant
                        startActivity(new Intent(this, PlaceActivity.class));
                        break;

                    case 1:     // search nearby place by place type and getLength
                        openGPS(LOCATE_FOR_NEARBY);
                        break;
                    case 2:     // weather
                    {
                        startActivity(new Intent(this, WeatherActivity.class));
                        break;
                    }

                    case 3:
                        startActivity(new Intent(this, PetrolActivity.class));
                        break;

                    case 4:     //share
                        openGPS(LOCATO_TO_SHARE);
                        break;
                }
                break;
            }

            case 1:
                switch (childPosition)
                {
                    case 0:     // load traffic jam
                        openGPS(LOCATE_TO_NOTIFY);
                        break;

                    case 1:     // notify traffic jam
                        loadTraffic();
                        break;

                    case 2:
                        startActivity(new Intent(this, ShortcutActivity.class));
                        break;
                }
                break;

            case 2:
            {
                switch (childPosition)
                {
                    case 0:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2:
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                }
                break;
            }

        }
        drawerLayout.closeDrawer(lvLeftmenu);
        return false;
    }

    void openGPS(int requestCode)
    {
        request = requestCode;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestLocation(requestCode);
            return;
        }
        if (ServiceUtils.checkServiceEnabled(this))
        {
            prbLoading.setVisibility(View.VISIBLE);
            map.setMyLocationEnabled(true);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            //Toast.makeText(this, getResources().getString(R.string.gps_loading), Toast.LENGTH_SHORT).show();
            //Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.gps_unabled), Toast.LENGTH_SHORT).show();
        }
    }

    void loadTraffic()
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        int time = TimeUtils.toMinutes(format.format(new Date()));
        int t = time / 30, tDown = t * 30, tUp = (t + 1) * 30;
        if ((tUp >= 390 && tUp <= 720) || (tUp >= 990 && tUp <= 1170))
        {
            prbLoading.setVisibility(View.VISIBLE);
            final Firebase firebase = new Firebase(getResources().getString(R.string.database_traffic));
            final Firebase timeNode = firebase.child(Integer.toString(tUp));
            final ValueEventListener listener = new ValueEventListener()    // chưa load downNode
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    prbLoading.setVisibility(View.VISIBLE);
                    //DataSnapshot upNode = snapshot.child(Integer.toString(tUp));
                    //DataSnapshot downNode = snapshot.child(Integer.toString(tDown));

                    trafficCircles = new ArrayList<>();
                    trafficCircles.addAll(getTrafficCircle(snapshot));
                    //trafficCircles.addAll(getTrafficCircle(upNode));
                    //trafficCircles.addAll(getTrafficCircle(downNode));

                    trafficLine = new ArrayList<>();
                    trafficLine.addAll(getTrafficLine(snapshot));
                    //trafficLine.addAll(getTrafficLine(upNode));
                    //trafficLine.addAll(getTrafficLine(downNode));

                    Log.d("traffic", "line size" + trafficLine.size());
                    Log.d("traffic", "circle size" + trafficCircles.size());


                    if (trafficLine.size() > 0)
                    {
                        map.clear();
                        int meta = ((Long) snapshot.child("meta").getValue()).intValue();
                        AddTrafficAst asyncTask = new AddTrafficAst(trafficLine, trafficCircles, map);
                        asyncTask.setListener(new OnLoadListener<HashMap<String, TrafficType>>()
                        {
                            @Override
                            public void onFinish(HashMap<String, TrafficType> result)
                            {
                                hmMarker = result;
                                setTrafficClick(true);
                                prbLoading.setVisibility(View.GONE);
                            }
                        });
                        asyncTask.execute(meta, getResources().getColor(R.color.yellowLight), getResources().getColor(R.color.redLight));
                    }
                    else
                    {
                        map.clear();
                        prbLoading.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Chưa có nơi nào tắc đường", Toast.LENGTH_SHORT).show();
                    }
                    timeNode.removeEventListener(this);

                }

                @Override
                public void onCancelled(FirebaseError firebaseError)
                {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                    prbLoading.setVisibility(View.GONE);
                    timeNode.removeEventListener(this);
                }
            };
            timeNode.addValueEventListener(listener);
        }
        else
        {
            Toast.makeText(this, "Hiện chưa có điểm kẹt xe nào", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setInterval(100); // Update location every second

        openGPS(LOCATE_ON_START);
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
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (myLocation != null)
        {
            SharedPreferences.Editor editor = getSharedPreferences("myLocation", MODE_PRIVATE).edit();
            editor.putString("myLat", Double.toString(myLocation.latitude));
            editor.putString("myLng", Double.toString(myLocation.longitude));
            editor.apply();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());

        switch (request)
        {
            case LOCATE_ON_START:
            {
                prbLoading.setVisibility(View.GONE);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
                break;
            }

            case LOCATE_ON_REQUEST:
            {
                AddressAst asyncTask = new AddressAst(geocoder);
                asyncTask.setListener(new OnLoadListener<String>()
                {
                    @Override
                    public void onFinish(String address)
                    {
                        prbLoading.setVisibility(View.GONE);
                        txtSearch.setText(address);
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
                        map.addMarker(new MarkerOptions().icon(icon).position(new LatLng(myLocation.latitude, myLocation.longitude)).title(address).snippet(""));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
                        setFavouriteClick();
                    }
                });
                asyncTask.execute(myLocation.latitude, myLocation.longitude);
                break;
            }

            case LOCATE_FOR_NEARBY:
                prbLoading.setVisibility(View.GONE);
                showDialog();
                break;

            case LOCATE_TO_NOTIFY:
            {
                prbLoading.setVisibility(View.GONE);
                Intent intent = new Intent(this, NotifyActivity.class);
                intent.putExtra("myLocation", myLocation);
                startActivityForResult(intent, LOCATE_TO_NOTIFY, null);
                break;
            }

            case LOCATO_TO_SHARE:
            {
                AddressAst asyncTask = new AddressAst(geocoder);
                asyncTask.setListener(new OnLoadListener<String>()
                {
                    @Override
                    public void onFinish(String address)
                    {
                        prbLoading.setVisibility(View.GONE);
                        if (address.length() > 0)
                        {
                            String msg = "Tôi đang ở địa chỉ " + address;
                            /*if (place.length() > 0)
                            {
                                msg = "Tôi đang ở " + place + ".\nĐịa chỉ " + address;
                            }
                            else
                            {
                                msg = "Tôi đang ở địa chỉ " + address;
                            }*/
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ từ ứng dụng Map Assistant.");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, msg);
                            startActivity(Intent.createChooser(sharingIntent, "Chia sẻ địa điểm"));
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Hãy chọn địa điểm trước", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                asyncTask.execute(myLocation.latitude, myLocation.longitude);
                break;
            }
        }
    }

    void loadAddress(final LatLng position)
    {

    }

    void setFavouriteClick()
    {
        destination = null;
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                marker.showInfoWindow();
                destination = new Place(marker.getPosition().latitude, marker.getPosition().longitude, marker.getTitle(), marker.getSnippet());
                Snackbar.make(root, marker.getTitle(), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Lưu", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                final String place = marker.getTitle();
                                if (place.length() > 1)
                                {
                                    final Dialog dialog = new Dialog(MainActivity.this);
                                    dialog.setContentView(R.layout.dialog_save_favourite);
                                    dialog.setTitle("Lưu vào yêu thích với tên ");

                                    final EditText txtFavourite = (EditText) dialog.findViewById(R.id.txtFavourite);
                                    txtFavourite.setText(place);
                                    Button btnSave = (Button) dialog.findViewById(R.id.btnSave);
                                    btnSave.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            String name = txtFavourite.getText().toString();
                                            if (name.length() > 1)
                                            {
                                                //saveFavourite(name, marker.getSnippet(), marker.getPosition());
                                                sqlite.delete("Favourite", name);
                                                sqlite.insert("Favourite", name, marker.getSnippet(), marker.getPosition().latitude, marker.getPosition().longitude);
                                                Toast.makeText(getApplicationContext(), "Đã lưu vào Yêu thích", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                            else
                                            {
                                                Toast.makeText(getApplicationContext(), "Bạn chưa đặt tên cho địa chỉ này", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    dialog.show();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Hãy chọn địa điểm trước", Toast.LENGTH_SHORT).show();
                                }
                                //saveFavourite(marker.getTitle());
                            }
                        }).setActionTextColor(getResources().getColor(R.color.colorPrimaryLight)).show();
                return true;
            }
        });
    }

    void setTrafficClick(final boolean myTraffic)
    {
        destination = null;
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                marker.showInfoWindow();
                prbLoading.setVisibility(View.VISIBLE);
                AddressAst asyncTask = new AddressAst(geocoder);
                asyncTask.setListener(new OnLoadListener<String>()
                {
                    @Override
                    public void onFinish(String address)
                    {
                        prbLoading.setVisibility(View.GONE);
                        Snackbar.make(root, address, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Đề xuất\nđường tắt", new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        TrafficType traffic = hmMarker.get(marker.getId());
                                        if (myTraffic)
                                        {
                                            TrafficCircle circle = trafficCircles.get(traffic.getIndex());
                                            Intent intent = new Intent(MainActivity.this, ShortcutActivity.class);
                                            intent.putExtra("jamType", false);
                                            intent.putExtra("jam", circle);
                                            intent.putExtra("ID", circle.getRating());
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            if (traffic.isLine())
                                            {
                                                TrafficLine line = trafficLine.get(traffic.getIndex());
                                                int ID = sqlite.haveStucked(line);
                                                if (ID != -1)
                                                {
                                                    Intent intent = new Intent(MainActivity.this, ShortcutActivity.class);
                                                    intent.putExtra("jamType", traffic.isLine());
                                                    intent.putExtra("jam", line);
                                                    intent.putExtra("ID", ID);
                                                    startActivity(intent);
                                                }
                                                else
                                                {
                                                    Toast.makeText(MainActivity.this, "Bạn chưa bao giờ thông báo kẹt xe ở đây", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else
                                            {
                                                TrafficCircle circle = trafficCircles.get(traffic.getIndex());
                                                int ID = sqlite.haveStucked(circle);
                                                if (ID != -1)
                                                {
                                                    Intent intent = new Intent(MainActivity.this, ShortcutActivity.class);
                                                    intent.putExtra("jamType", traffic.isLine());
                                                    intent.putExtra("jam", circle);
                                                    intent.putExtra("ID", ID);
                                                    startActivity(intent);
                                                }
                                                else
                                                {
                                                    Toast.makeText(MainActivity.this, "Bạn chưa bao giờ thông báo kẹt xe ở đây", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                                })
                                .setActionTextColor(getResources().getColor(R.color.green)).show();
                    }
                });
                asyncTask.execute(marker.getPosition().latitude, marker.getPosition().longitude);
                //loadAddress(marker.getPosition());
                return false;
            }
        });
    }
}


