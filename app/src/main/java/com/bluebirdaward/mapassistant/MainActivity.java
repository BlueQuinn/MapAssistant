package com.bluebirdaward.mapassistant;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import adapter.MenuAdt;
import asyncTask.AddTrafficAst;
import asyncTask.AddressAst;
import asyncTask.FindPlaceAst;
import model.MenuSection;
import model.Menu;
import model.Nearby;
import model.Place;
import model.Traffic;
import listener.OnLoadListener;
import sqlite.SqliteHelper;
import utils.RequestCode;
import utils.ServiceUtils;
import widgets.PlacePickerDialog;

import com.bluebirdaward.mapassistant.gmmap.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, View.OnLongClickListener,
        ExpandableListView.OnChildClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener

{
    final int LOCATE_ON_START = 1;      //
    final int LOCATE_ON_REQUEST = 2;    // btnTrack click
    final int LOCATE_FOR_NEARBY = 3;      //
    final int LOCATE_FOR_DIRECTION = 4;    //
    final int LOCATE_TO_NOTIFY = 5;

    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    ProgressBar prbLoading;
    LatLng myLocation, destination;

    //ImageButton btnMenu, btnVoice;
    TextView txtSearch;
    String place = "", address = "";
    public static SqliteHelper dbHelper;

    ExpandableListView lvLeftmenu;
    DrawerLayout drawerLayout;

    int request = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        FloatingActionButton btnTrack = (FloatingActionButton) findViewById(R.id.btnTrack);
        FloatingActionButton btnFavourite = (FloatingActionButton) findViewById(R.id.btnFavourite);
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

        initMenu();
        //setAdapter();
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
        listMenu.add(new Menu("Tìm đường đi", R.drawable.direction));
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
        dbHelper = new SqliteHelper(getApplicationContext(), "Destination.sqlite");
        try
        {
            MainActivity.dbHelper.createDataBase();
            MainActivity.dbHelper.openDataBase();
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

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
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

        //if (myLat.length() > 0 && myLng.length() > 0)
        //{
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(MainActivity.this, "Bạn chưa bật GPS Location", Toast.LENGTH_SHORT).show();
            requestLocation(LOCATE_ON_START);
            return;
        }
        //}
        //map.setMyLocationEnabled(true);
        //setMap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (data != null)
        {
            map.clear();
            switch (requestCode)
            {
                case RequestCode.SEARCH_DESTINATION:
                {
                    place = data.getStringExtra("place");
                    address = data.getStringExtra("address");
                    txtSearch.setText(place);

                    LatLng position = data.getParcelableExtra("position");
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16));

                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                    {
                        @Override
                        public boolean onMarkerClick(final Marker marker)
                        {
                            Snackbar.make(findViewById(R.id.frameLayout), marker.getTitle(), Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Chỉ đường", new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            destination = marker.getPosition();
                                            openGPS(LOCATE_FOR_DIRECTION);
                                        }
                                    }).show();
                            return true;
                        }
                    });
                    map.addMarker(new MarkerOptions().icon(icon).position(position).title(place).snippet(address));
                    break;
                }

                case RequestCode.VOICE_SEARCH:
                {
                    if (resultCode == RESULT_OK)
                    {
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        txtSearch.setText(result.get(0));
                        startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", txtSearch.getText().toString()), RequestCode.SEARCH_DESTINATION);
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
                /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    requestLocation(LOCATE_ON_REQUEST);
                    return;
                }*/
                openGPS(LOCATE_ON_REQUEST);
                break;
            }

            case R.id.txtSearch:
                startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", txtSearch.getText().toString()), RequestCode.SEARCH_DESTINATION);
                break;

            case R.id.btnFavourite:
            {
                if (address.length() > 1)
                {
                    final Dialog dialog = new Dialog(this);
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
                            if (name.length() > 0)
                            {
                                saveFavourite(name);
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
                break;
            }

            case R.id.btnVoice:
            {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bạn cần tìm gì ?");
                startActivityForResult(intent, RequestCode.VOICE_SEARCH);
                break;
            }

            case R.id.btnMenu:
                drawerLayout.openDrawer(lvLeftmenu);
                break;
        }
    }

    void saveFavourite(String name)
    {
        dbHelper.delete("Favourite", name);
        dbHelper.insert("Favourite", name, address, destination.latitude, destination.longitude);
        Toast.makeText(getApplicationContext(), "Đã lưu vào Yêu thích", Toast.LENGTH_SHORT).show();
    }

    void showDialog()
    {
        final GoogleMap.OnMarkerClickListener listener = new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                marker.hideInfoWindow();

                place = marker.getTitle();
                address = marker.getSnippet();
                destination = marker.getPosition();     // consider reference ???

                Snackbar.make(findViewById(R.id.frameLayout), marker.getTitle(), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Chỉ đường", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                //destination = marker.getPosition();
                                Intent intent = new Intent(MainActivity.this, DirectionActivity.class);
                                intent.putExtra("request", DirectionActivity.PLACE_DIRECTION);
                                intent.putExtra("myLocation", myLocation);
                                intent.putExtra("destination", destination);
                                intent.putExtra("place", place);
                                intent.putExtra("address", address);
                                startActivity(intent);
                            }
                        }).show();
                return false;
            }
        };

        final PlacePickerDialog dialog = new PlacePickerDialog(this);
        dialog.setOnPickListener(new OnLoadListener<Nearby>()
        {
            @Override
            public void onFinish(Nearby nearby)
            {
                final int radius = nearby.getRadius();
                FindPlaceAst asyncTask = new FindPlaceAst(getApplicationContext(), nearby.getPlaceType(), radius);
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
                        map.addCircle(new CircleOptions().center(myLocation)
                                .radius(1000 * radius).strokeColor(Color.RED));
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                        map.setOnMarkerClickListener(listener);
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
                    {
                        Intent i = new Intent(this, PlaceActivity.class);
                        startActivity(i);
                        break;
                    }
                    case 1:     // search nearby place by place type and radius
                        openGPS(LOCATE_FOR_NEARBY);
                        break;
                    case 2:     // direction
                    {
                        Intent intent = new Intent(this, DirectionActivity.class);
                        intent.putExtra("position", map.getCameraPosition().target);
                        intent.putExtra("zoom", map.getCameraPosition().zoom);
                        startActivity(intent);
                        break;
                    }

                    case 3:
                        if (address.length() > 0)
                        {
                            String msg;
                            if (place.length() > 0)
                            {
                                msg = "Tôi đang ở " + place + ".\nĐịa chỉ " + address;
                            }
                            else
                            {
                                msg = "Tôi đang ở địa chỉ " + address;
                            }
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
        prbLoading.setVisibility(View.VISIBLE);
        //Toast.makeText(getApplicationContext(), "Đang tải dữ liệu", Toast.LENGTH_SHORT).show();
        final Firebase ref = new Firebase(getResources().getString(R.string.database_traffic));
        final ValueEventListener listener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                //prbLoading.setVisibility(View.VISIBLE);
                try
                {
                    final ArrayList<Traffic> listTraffic = new ArrayList<>();
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date date = new Date();
                    int timeNow = 60 * date.getHours() + date.getMinutes();

                    int meta = ((Long) snapshot.child("meta").getValue()).intValue();
                    DataSnapshot traffic = snapshot.child("traffic");
                    for (DataSnapshot item : traffic.getChildren())
                    {
                        //ArrayList<Jam> jamList = new ArrayList<>();
                        int vote = 0;
                        DataSnapshot jamData = item.child("jam");
                        for (DataSnapshot jam : jamData.getChildren())
                        {
                            String time = (String) jam.child("time").getValue();
                            Date jamTime = formatter.parse(time);
                            int span = timeNow - 60 * jamTime.getHours() - jamTime.getMinutes();
                            if (span > -90 && span < 90)   // timespan between 90 minutes earlier or later
                            {
                                vote = ((Long) jam.child("vote").getValue()).intValue();
                                if (vote > meta)
                                {
                                    break;
                                }
                                vote = 0;
                            }
                        }

                        //Log.d("jam", "jam size = " + jamList.size());
                        if (vote > 0)
                        {
                            double lat = (double) item.child("position/lat").getValue();
                            double lng = (double) item.child("position/lng").getValue();
                            listTraffic.add(new Traffic(lat, lng, vote));
                        }
                    }
                    //Log.d("123", "" + listTraffic.size());

                    if (listTraffic.size() > 0)
                    {
                        map.clear();
                        AddTrafficAst asyncTask = new AddTrafficAst(listTraffic, map);
                        asyncTask.setListener(new OnLoadListener<Boolean>()
                        {
                            @Override
                            public void onFinish(Boolean result)
                            {
                                final Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                                {
                                    @Override
                                    public boolean onMarkerClick(Marker marker)
                                    {
                                        prbLoading.setVisibility(View.VISIBLE);
                                        AddressAst asyncTask = new AddressAst(geocoder);
                                        asyncTask.setListener(new OnLoadListener<String>()
                                        {
                                            @Override
                                            public void onFinish(String address)
                                            {
                                                prbLoading.setVisibility(View.GONE);
                                                Snackbar.make(findViewById(R.id.frameLayout), address, Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("Giải pháp", new View.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(View v)
                                                            {

                                                            }
                                                        })
                                                        .show();
                                            }
                                        });
                                        LatLng position = marker.getPosition();
                                        asyncTask.execute(position.latitude, position.longitude);
                                        return false;
                                    }
                                });
                                prbLoading.setVisibility(View.GONE);
                            }
                        });
                        asyncTask.execute(meta);
                    }
                    else
                    {
                        prbLoading.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Chưa có nơi nào tắc đường", Toast.LENGTH_SHORT).show();
                    }
                    ref.removeEventListener(this);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
                System.out.println("The read failed: " + firebaseError.getMessage());
                prbLoading.setVisibility(View.GONE);
                ref.removeEventListener(this);
            }
        };
        ref.addValueEventListener(listener);
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
                AddressAst asyncTask = new AddressAst(new Geocoder(this, Locale.getDefault()));
                asyncTask.setListener(new OnLoadListener<String>()
                {
                    @Override
                    public void onFinish(String result)
                    {
                        prbLoading.setVisibility(View.GONE);
                        destination = new LatLng(myLocation.latitude, myLocation.longitude);
                        address = result;
                        txtSearch.setText(address);
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
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
                startActivity(intent);
                break;
            }

            case LOCATE_FOR_DIRECTION:
            {
                prbLoading.setVisibility(View.GONE);
                Intent intent = new Intent(MainActivity.this, DirectionActivity.class);
                intent.putExtra("request", DirectionActivity.PLACE_DIRECTION);
                intent.putExtra("myLocation", myLocation);
                intent.putExtra("destination", destination);
                intent.putExtra("place", place);
                intent.putExtra("address", address);
                startActivity(intent);
                break;
            }
        }
    }
}


