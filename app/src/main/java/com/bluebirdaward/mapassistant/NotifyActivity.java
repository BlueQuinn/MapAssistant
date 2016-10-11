package com.bluebirdaward.mapassistant;

import android.app.Dialog;
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
import listener.DetectTrafficListener;
import model.Jam;
import listener.OnLoadListener;
import model.MyTraffic;
import model.Route;
import model.Shortcut;
import utils.MapUtils;
import utils.PolyUtils;
import utils.RequestCode;
import widgets.LoadingDialog;
import widgets.MessageDialog;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.LatLng;

import static utils.TimeUtils.*;

public class NotifyActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener,
        ValueEventListener

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
        //myLocation = intent.getParcelableExtra("myLocation");
        myLocation = new LatLng(10.769914, 106.670608);

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvRadius = (TextView) findViewById(R.id.tvRadius);
        btnNotify = (Button) findViewById(R.id.btnNotify);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        layoutContent = (LinearLayout) findViewById(R.id.main_content);
        radiusPicker = (SeekBar) findViewById(R.id.radiusPicker);
        radiusPicker.setProgress(4);    // default 200m
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
                return false;
            }
        }
        return false;
    }

    boolean send = false;
    Firebase ref;
    LoadingDialog dialog;

    void saveFirebase()
    {
        dialog = new LoadingDialog(this);
        dialog.show();
        //prbLoading.setVisibility(View.VISIBLE);
        ref = new Firebase(getResources().getString(R.string.database_traffic));
        ref.addValueEventListener(this);
    }
    /*void saveFirebase()
    {
        MainActivity.dbHelper.saveLocation(myLocation.latitude, myLocation.longitude);
        showMessage("Thông báo của bạn đã được gửi đi");
    }*/

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
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;

            case R.id.info:
                showMessage(info);
                break;

            case R.id.mytraffic:
            {
                //Intent intent = new Intent();
                setResult(RequestCode.LOCATE_TO_NOTIFY, new Intent());
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    int getLength()
    {
        return (radiusPicker.getProgress() + 4) * 50;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        tvRadius.setText("Ước tính phạm vi ùn tắc: " + ((progress + 4) * 50) + "m");
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

    @Override
    public void onDataChange(DataSnapshot snapshot)
    {
        send = false;
        final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        //final Date date = new Date();
        String date = formatter.format(new Date());
        final int timeNow = toMinutes(date);

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
                        send = true;
                        break;
                    }
                }
                if (send)
                {
                    break;
                }
            }
        }

        if (send)
        {
            MessageDialog messageDialog = new MessageDialog(NotifyActivity.this);
            messageDialog.show();
            MainActivity.dbHelper.saveLocation(myLocation.latitude, myLocation.longitude);
            ref.removeEventListener(this);
        }
        else
        {
            MapUtils utils = new MapUtils(getLength() / 2);
            utils.getRoad(getResources().getString(R.string.google_maps_key), myLocation, new DetectTrafficListener()
            {
                @Override
                public void onFinish(Route route, int i, LatLng[] intersection)
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

                        Intent intent = new Intent();
                        intent.putExtra("route", route);
                        intent.putExtra("lat1", intersection[0].latitude);
                        intent.putExtra("lng1", intersection[0].longitude);
                        intent.putExtra("lat2", intersection[1].latitude);
                        intent.putExtra("lng2", intersection[1].longitude);
                        intent.putExtra("i", i);
                        setResult(RequestCode.LOCATE_TO_NOTIFY, intent);

                        MessageDialog messageDialog = new MessageDialog(NotifyActivity.this);
                        messageDialog.show();
                        MainActivity.dbHelper.saveLocation(myLocation.latitude, myLocation.longitude);
                        //finish();
                    }
                    else
                    {
                        showMessage("Không thể gửi được thông báo này");
                    }
                    ref.removeEventListener(NotifyActivity.this);
                }
            });
        }
        dialog.dismiss();
    }

    @Override
    public void onCancelled(FirebaseError firebaseError)
    {

    }
}
