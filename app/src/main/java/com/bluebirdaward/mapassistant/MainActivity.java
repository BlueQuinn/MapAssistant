package com.bluebirdaward.mapassistant;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import Adapter.MenuAdt;
import AsyncTask.FindPlaceAst;
import DTO.Jam;
import DTO.MenuSection;
import DTO.Menu;
import DTO.Place;
import DTO.Traffic;
import Listener.OnLoadListener;
import Sqlite.SqliteHelper;
import Utils.AddressUtils;
import Utils.RequestCode;

import com.bluebirdaward.mapassistant.gmmap.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, View.OnLongClickListener, GoogleMap.OnMyLocationChangeListener,
        ExpandableListView.OnChildClickListener
{
    /*InterstitialAd mInterstitialAd;
    Handler mHandler;       // Handler to display the ad on the UI thread
    private Runnable displayAd;     // Code to execute to perform this operation*/

    GoogleMap map;

    FloatingActionButton btnTrack, btnFavourite;
    ProgressBar prbLoading;
    public static LatLng myLocation;
    ImageButton btnMenu, btnVoice;
    TextView txtSearch;
    String place = "", address = "";
    public static SqliteHelper dbHelper;

    ExpandableListView lvLeftmenu;
    MenuAdt adapter;

    FrameLayout frameLayout;
    ArrayList<MenuSection> listSection = new ArrayList<>();
    DrawerLayout drawerLayout;

    int choice = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        btnTrack = (FloatingActionButton) findViewById(R.id.btnTrack);
        btnFavourite = (FloatingActionButton) findViewById(R.id.btnFavourite);
        btnMenu = (ImageButton) findViewById(R.id.btnMenu);
        btnVoice = (ImageButton) findViewById(R.id.btnVoice);
        txtSearch = (TextView) findViewById(R.id.txtSearch);
        lvLeftmenu = (ExpandableListView) findViewById(R.id.lvLeftMenu);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);

        btnTrack.setOnClickListener(this);
        btnFavourite.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        txtSearch.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        prbLoading.setVisibility(View.GONE);


        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
// enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
// enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
// set the transparent color of the status bar, 20% darker
        tintManager.setTintColor(Color.parseColor("#20000000"));*/

        initMenu();
        setAdapter();
        initDatabase();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String myLat = sharedPref.getString("myLat", "");
        String myLng = sharedPref.getString("myLng", "");
        if (myLat.length() < 1 || myLng.length() < 1)
        //myLocation = new LatLng(10.762689, 106.68233989999999);
        {
            myLocation = new LatLng(10.78261522192309, 106.69588862681348);
        }
        else
        {
            myLocation = new LatLng(Double.parseDouble(myLat), Double.parseDouble(myLng));
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_interstitial));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        mHandler = new Handler(Looper.getMainLooper());
        displayAd = new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                    }
                });
            }
        };*/
    }

   /* void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void displayInterstitial() {
        mHandler.postDelayed(displayAd, 1);
    }*/

    void initMenu()
    {
        ArrayList<Menu> listMenu = new ArrayList<>();

        listMenu.add(new Menu("Tìm nhà hàng", R.drawable.restaurant));
        listMenu.add(new Menu("Tìm địa điểm", R.drawable.place));
        listMenu.add(new Menu("Tìm đường đi", R.drawable.direction));
        listMenu.add(new Menu("Tránh tắc đường", R.drawable.traffic_cone));
        listMenu.add(new Menu("Thông báo tắc đường", R.drawable.warning));
        listMenu.add(new Menu("Chia sẻ", R.drawable.share));
        listSection.add(new MenuSection("Tiện ích", listMenu));

        listMenu = new ArrayList<>();
        listMenu.add(new Menu("Bản đồ thường", R.drawable.normal));
        listMenu.add(new Menu("Xem từ vệ tinh", R.drawable.satellite));
        listMenu.add(new Menu("Xem theo địa hình", R.drawable.terrain));
        listSection.add(new MenuSection("Xem bản đồ", listMenu));

        //listMenu = new ArrayList<>();
        //listMenu.add(new Menu("Facebook", R.drawable.facebook));
        //listSection.add(new MenuSection("Chia sẻ", listMenu));

        /*if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }*/
    }

    void setAdapter()
    {
        adapter = new MenuAdt(getApplicationContext(), R.layout.row_menu, R.layout.row_section, listSection);

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

    void setMap()
    {
        map.setContentDescription("");
        map.setTrafficEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this, "Bạn chưa bật GPS Location", Toast.LENGTH_SHORT).show();
            requestLocation(1);
            return;
        }
        map.setMyLocationEnabled(true);
        setMap();
    }

    @Override
    public void onMyLocationChange(Location location)
    {
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        address = AddressUtils.getAddress(new Geocoder(this, Locale.getDefault()), myLocation.latitude, myLocation.longitude);
        txtSearch.setText(address);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, map.getCameraPosition().zoom));
        map.setOnMyLocationChangeListener(null);
        Log.d("123", "my = " + myLocation.latitude + " " + myLocation.longitude);

        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("myLat", Double.toString(location.getLatitude()));
        editor.putString("myLng", Double.toString(location.getLongitude()));
        editor.apply();
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

                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
                    map.addMarker(new MarkerOptions().icon(icon).position(position));
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
        switch (requestCode)
        {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        map.setMyLocationEnabled(true);
                        setMap();
                    }
                }
                return;
            }
            case 2:
            {
                if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    map.setMyLocationEnabled(true);
                    setMap();
                    map.setOnMyLocationChangeListener(this);
                }
                return;
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    requestLocation(2);
                    return;
                }
                map.setOnMyLocationChangeListener(this);
                break;
            }

            case R.id.txtSearch:
                startActivityForResult(new Intent(this, DestinationActivity.class).putExtra("address", txtSearch.getText().toString()), RequestCode.SEARCH_DESTINATION);
                break;

            case R.id.btnFavourite:
            {
                if (address.length() > 1)
                {
                    if (place.length() < 1)
                    {
                        final Dialog dialog = new Dialog(this);
                        dialog.setContentView(R.layout.dialog_save_favourite);
                        dialog.setTitle("Lưu vào yêu thích với tên ");

                        final EditText txtFavourite = (EditText) dialog.findViewById(R.id.txtFavourite);
                        Button btnSave = (Button) dialog.findViewById(R.id.btnSave);
                        btnSave.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                String name = txtFavourite.getText().toString();
                                if (name.length() > 0)
                                {
                                    place = name;
                                    saveFavourite();
                                    place = "";
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
                        saveFavourite();
                    }
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

    void saveFavourite()
    {
        dbHelper.delete("Favourite", place);
        dbHelper.insert("Favourite", place, address);
        Toast.makeText(getApplicationContext(), "Đã lưu vào Yêu thích", Toast.LENGTH_SHORT).show();
    }

    void showDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_place_picker);

        final AutoCompleteTextView autoCompleteSearch = (AutoCompleteTextView) dialog.findViewById(R.id.auto_complete_search);

        String[] places = {"Accounting", "Airport", "Amusement Park", "Aquarium", "Art Gallery", "Atm", "Bakery", "Bank", "Bar", "Beauty Salon", "Bicycle Store", "Book Store", "Bus Station", "Cafe", "Campground", "Car Dealer", "Car Rental", "Car Repair", "Car Wash", "Casino", "Cemetery", "Church", "City Hall", "Clothing Store", "Convenience Store", "Courthouse", "Dentist", "Department Store", "Doctor", "Electrician", "Electronics Store", "Embassy", "Finance", "Fire Station", "Florist", "Food", "Funeral Home", "Furniture Store", "Gas Station", "Grocery Or Supermarket", "Gym", "Hair Care", "Hardware Store", "Health", "Home Goods Store", "Hospital", "Insurance Agency", "Jewelry Store", "Laundry", "Lawyer", "Library", "Liquor Store", "Local Government Office", "Locksmith", "Lodging", "Meal Delivery", "Meal Takeaway", "Mosque", "Movie Rental", "Movie Theater", "Moving Company", "Museum", "Night Club", "Painter", "Park", "Parking", "Pet Store", "Pharmacy", "Plumber", "Police", "Post Office", "Real Estate Agency", "Restaurant", "School", "Shoe Store", "Shopping Mall", "Spa", "Stadium", "Storage", "Store", "Taxi Stand", "Train Station", "Travel Agency", "University", "Zoo"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, places);
        autoCompleteSearch.setAdapter(adapter);
        autoCompleteSearch.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                choice = position;
            }
        });

        Integer[] rad = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Spinner radiusPicker = (Spinner) dialog.findViewById(R.id.radius_picker);
        radiusPicker.setAdapter(new ArrayAdapter<Integer>(this, R.layout.row_spinner, rad)
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);
                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = super.getDropDownView(position, convertView, parent);
                return v;
            }
        });
        radiusPicker.setSelection(2);

        Button btnFind = (Button) dialog.findViewById(R.id.btnFind);
        btnFind.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (choice > -1)
                {
                    String placeType = adapter.getItem(choice).toLowerCase().replace(" ", "_");
                    FindPlaceAst asyncTask = new FindPlaceAst(getApplicationContext(), placeType, radiusPicker.getSelectedItemPosition() + 1);
                    asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<Place>>()
                    {
                        @Override
                        public void onLoaded(ArrayList<Place> list)
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
                            //ArrayList<Place> list = (ArrayList<Place>) result;
                            for (int i = 0; i < list.size(); ++i)
                            {
                                Place place = list.get(i);
                                LatLng position = new LatLng(place.getLat(), place.getLng());
                                map.addMarker(options.position(position).title(place.getName()));
                                builder.include(position);
                            }
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                        }
                    });
                    asyncTask.execute(myLocation.latitude, myLocation.longitude);

                    choice = -1;
                    dialog.dismiss();
                }
                else
                {
                }
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
                    case 1:     // search nearby place by place type
                        showDialog();
                        break;
                    case 2:     // direction
                    {
                        Intent intent = new Intent(this, DirectionActivity.class);
                        intent.putExtra("position", map.getCameraPosition().target);
                        intent.putExtra("zoom", map.getCameraPosition().zoom);
                        startActivity(intent);
                        break;
                    }
                    case 3:     // load traffic jam
                        loadTraffic();
                        break;

                    case 4:     // notify traffic jam
                    {
                        Intent intent = new Intent(this, NotifyActivity.class);
                        startActivity(intent);
                        break;
                    }

                    case 5:
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
                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ từ Map Assistant");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, msg);
                            startActivity(Intent.createChooser(sharingIntent, "message"));
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

            case 2:     // share
            {
                switch (childPosition)
                {
                    case 0:

                }
                break;
            }
        }
        drawerLayout.closeDrawer(lvLeftmenu);
        return false;
    }


    void loadTraffic()
    {
        //prbLoading.setVisibility(View.VISIBLE);
        //Toast.makeText(getApplicationContext(), "Đang tải dữ liệu", Toast.LENGTH_SHORT).show();
        final ArrayList<Traffic> listTraffic = new ArrayList<>();
        final Firebase ref = new Firebase(getResources().getString(R.string.trafficDatabase));
        ValueEventListener listener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                prbLoading.setVisibility(View.VISIBLE);
                try
                {
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
                    Log.d("123", "" + listTraffic.size());
                    markTraffic(listTraffic, meta);
                    prbLoading.setVisibility(View.GONE);
                    //ref.removeEventListener(listener);
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
                //ref.removeEventListener(listener);
            }
        };
        ref.addValueEventListener(listener);
    }

    void markTraffic(ArrayList<Traffic> listTraffic, int meta)
    {
        map.clear();
        BitmapDescriptor icon;
        MarkerOptions options;

        for (Traffic traffic : listTraffic)
        {
            if (traffic.getVote() < 2 * meta)
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_medium);
            }
            else
            {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_high);
            }
            options = new MarkerOptions().icon(icon);
            map.addMarker(options.position(new LatLng(traffic.getLat(), traffic.getLng())).title(AddressUtils.getAddress(new Geocoder(this, Locale.getDefault()), traffic.getLat(), traffic.getLng())));
        }
    }

    /*int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }*/
}


