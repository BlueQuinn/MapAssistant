package com.bluebirdaward.mapassistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import asyncTask.AddressAst;
import model.Jam;
import listener.OnLoadListener;
import model.Position;
import utils.MapUtils;
import utils.PolyUtils;
import widgets.LoadingDialog;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.LatLng;

public class NotifyActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener
{
    LatLng myLocation;
    Button btnNotify;
    LinearLayout layoutContent;
    ProgressBar prbLoading;
    TextView tvAddress, tvRadius;
    SeekBar radiusPicker;
String info;

    View.OnClickListener reloadListener, notifyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        Firebase.setAndroidContext(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Thông báo tắc đường");
        info = "Gửi thông tin về vị trí mà bạn đang bị tắc đường để mọi người có thể cập nhật tình trạng giao thông";
        Intent intent = getIntent();
        myLocation = intent.getParcelableExtra("myLocation");

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvRadius = (TextView) findViewById(R.id.tvRadius);
        btnNotify = (Button) findViewById(R.id.btnNotify);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        layoutContent = (LinearLayout) findViewById(R.id.main_content);
        radiusPicker = (SeekBar) findViewById(R.id.radiusPicker);
        radiusPicker.setProgress(100);    // default 200m
        radiusPicker.setOnSeekBarChangeListener(this);

        reloadListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loadMyLocation();
            }
        };

        notifyListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkLocation())
                {
                    showMessage("Không thể gửi thông báo !!!\nBạn đã từng thông báo ở vị trí này từ trước đó rồi");
                }
                else
                {
                    saveFirebase();
                }
                info = "Thông tin của bạn đang được server xử lý";
            }
        };

        loadMyLocation();
    }

    void loadMyLocation()
    {
        prbLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        AddressAst asyncTask = new AddressAst(new Geocoder(this, Locale.getDefault()));
        asyncTask.setListener(new OnLoadListener<String>()
        {
            @Override
            public void onFinish(String address)
            {
                prbLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);

                if (address.length() < 1)
                {
                    tvAddress.setText("Không có kết nối internet");
                    btnNotify.setText("Thử lại");
                    btnNotify.setOnClickListener(reloadListener);
                }
                else
                {
                    tvAddress.setText("Bạn đang ở " + address);
                    btnNotify.setText("Thông báo");
                    btnNotify.setOnClickListener(notifyListener);
                }
            }
        });
        asyncTask.execute(myLocation.latitude, myLocation.longitude);
    }

    float getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        Location homeLocation = new Location("");
        homeLocation.setLatitude(lat1);
        homeLocation.setLongitude(lng1);

        Location targetLocation = new android.location.Location("");
        targetLocation.setLatitude(lat2);
        targetLocation.setLongitude(lng2);

        return targetLocation.distanceTo(homeLocation);     // meters
    }

    int toMinutes(String time)
    {
        String[] hourMin = time.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int mins = Integer.parseInt(hourMin[1]);
        int hoursInMins = hour * 60;
        return hoursInMins + mins;
    }

    String toTime(int minutes)
    {
        int hour = minutes / 60;
        minutes = minutes - hour * 60;
        return Integer.toString(hour) + ":" + Integer.toString(minutes);
    }

    boolean checkLocation()
    {
        ArrayList<HashMap<String, String>> listRow = MainActivity.dbHelper.getAll("Location");
        //Log.d("123", "size " + listRow.size());
        for (HashMap<String, String> row : listRow)
        {
            double lat = Double.parseDouble(row.get("Lat"));
            double lng = Double.parseDouble(row.get("Lng"));
            float distance = getDistance(myLocation.latitude, myLocation.longitude, lat, lng);
            //Log.d("123", "lat = " + lat + " " + myLocation.latitude);
            //Log.d("123", "lng = " + lng + " " + myLocation.longitude);
            //Log.d("123", "distance = " + distance);
            if (distance >= 0 && distance <= 1000)
            {
                return true;
            }
        }
        return false;
    }

    /*boolean send= false;
    void saveFirebase()
    {
        final LoadingDialog dialog = new LoadingDialog(this);
        dialog.show();
        //prbLoading.setVisibility(View.VISIBLE);
        final Firebase ref = new Firebase(getResources().getString(R.string.database_traffic));
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                send = false;
                final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                //final Date date = new Date();
                String date = formatter.format(new Date());
                final int timeNow = toMinutes(date);

                boolean findPosition = false;
                final DataSnapshot traffic = snapshot.child("traffic");
                for (DataSnapshot item : traffic.getChildren())
                {
                    double lat1 = (double) item.child("lat1").getValue();
                    double lng1 = (double) item.child("lng1").getValue();
                    double lat2 = (double) item.child("lat2").getValue();
                    double lng2 = (double) item.child("lng2").getValue();
                    ArrayList<LatLng> polyline = new ArrayList<>();
                    polyline.add(new LatLng(lat1, lng1));
                    polyline.add(new LatLng(lat2, lng2));

                    if (PolyUtils.isLocationOnPath(myLocation, polyline, false, 100))     // kiểm tra xem trên Firebase đã tồn tại điểm nào ở gần điểm này mà bị tắc đường chưa (gần ở đây là dưới 300m)
                    {
                        findPosition = true;
                        //boolean findTime = false;
                        DataSnapshot jamList = item.child("jam");
                        for (DataSnapshot jamItem : jamList.getChildren())
                        {
                            String start = (String) jamItem.child("start").getValue();
                            String end = (String) jamItem.child("end").getValue();
                            int startMins = toMinutes(start);
                            int endMins = toMinutes(end);
                            if (timeNow >= startMins && timeNow <= endMins)
                            {
                                int vote = ((Long) jamItem.child("vote").getValue()).intValue();
                                Firebase jamRef = jamItem.getRef();
                                Map<String, Object> voteMap = new HashMap<>();
                                voteMap.put("vote", vote + 1);
                                jamRef.updateChildren(voteMap);
                                //findTime = true;
                                break;
                            }
                        }
                        *//*if (!findTime)
                        {
                            Firebase trafficRef = item.getRef();
                            Map<String, Object> jam = new HashMap<>();
                            jam.put("time", formatter.format(date));
                            jam.put("vote", 1);
                            trafficRef.push().setValue(jam);
                        }*//*
                        break;
                    }
                }
                if (!findPosition)
                {
                    MapUtils.getRoad(getResources().getString(R.string.google_maps_key), myLocation, radiusPicker.getProgress() + 100, new OnLoadListener<LatLng[]>()
                    {
                        @Override
                        public void onFinish(LatLng[] intersection)
                        {
                            if (intersection != null && intersection.length == 2)
                            {
                                Firebase trafficRef = traffic.getRef();
                                //Strin start date.getTime() / 1000;
                                Jam[] jam = new Jam[]{new Jam(toTime(timeNow - 30), toTime(timeNow + 30), 1)};
                                Map<String, Object> trafficItem = new HashMap<>();
                                trafficItem.put("lat1", intersection[0].latitude);
                                trafficItem.put("lng1", intersection[0].longitude);
                                trafficItem.put("lat2", intersection[1].latitude);
                                trafficItem.put("lng2", intersection[1].longitude);
                                trafficItem.put("jam", jam);
                                trafficRef.push().setValue(trafficItem);
                                send = true;
                            }
                        }
                    });
                }
                dialog.dismiss();

                //MainActivity.dbHelper.saveLocation(myLocation.latitude, myLocation.longitude);
                if (send)
                showMessage("Thông báo của bạn đã được gửi đi");
                else
                    showMessage("Không thể gửi được thông báo này");
                ref.removeEventListener(this);
                //prbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
                System.out.println("The read failed: " + firebaseError.getMessage());
                ref.removeEventListener(this);
            }
        });
    }*/


    void saveFirebase()
    {
        MainActivity.dbHelper.saveLocation(myLocation.latitude, myLocation.longitude);
            showMessage("Thông báo của bạn đã được gửi đi");
        //ref.removeEventListener(this);
        //prbLoading.setVisibility(View.GONE);
    }

    void showMessage(String message)
    {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        //finish();
                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        else        // info
        {
            showMessage(info);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        tvRadius.setText("Ước tính phạm vi ùn tắc: " + (progress + 100) + "m");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_notify, menu);
        return true;
    }
}
