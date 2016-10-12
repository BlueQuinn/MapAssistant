package com.bluebirdaward.mapassistant;

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
import listener.OnLoadListener;
import model.TrafficCircle;
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
                    saveTraffic();
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
        ArrayList<HashMap<String, String>> listRow = MainActivity.sqlite.getAll("Location");
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

    void saveTraffic()
    {
        dialog = new LoadingDialog(this);
        dialog.show();

        String date = new SimpleDateFormat("HH:mm").format(new Date());
        final int timeNow = toMinutes(date);
        int t = timeNow / 30, tDown = t * 30, tUp = (t + 1) * 30;
        int time;
        if (timeNow - tDown < tUp - timeNow)
        {
            time = tDown;
        }
        else
        {
            time = tUp;
        }
        Log.d("time", "" + time);
        ref = new Firebase(getResources().getString(R.string.database_traffic)).child(Integer.toString(time));
        ref.addValueEventListener(this);
    }
    /*void saveTraffic()
    {
        MainActivity.dbHelper.saveTraffic(myLocation.latitude, myLocation.longitude);
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
        tvRadius.setText("Ước tính phạm vi ùn tắc: " + TrafficCircle.getRadius(progress) + "m");
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
        //send = false;
        boolean find = false;
        DataSnapshot lineData = snapshot.child("line");
        for (DataSnapshot i : lineData.getChildren())
        {
            double lat1 = (double) i.child("lat1").getValue();
            double lng1 = (double) i.child("lng1").getValue();
            double lat2 = (double) i.child("lat2").getValue();
            double lng2 = (double) i.child("lng2").getValue();
            ArrayList<LatLng> list = new ArrayList<>();
            list.add(new LatLng(lat1, lng1));
            list.add(new LatLng(lat2, lng2));
            if (PolyUtils.isLocationOnPath(myLocation, list, false, 100))
            {
                int rate = ((Long) i.child("rate").getValue()).intValue();
                Firebase ref = i.getRef();
                Map<String, Object> rateNode = new HashMap<>();
                rateNode.put("rate", rate + 1);
                ref.updateChildren(rateNode);
                find = true;
                break;
            }
        }

        DataSnapshot circleData = snapshot.child("circle");
        if (!find)
        {
            for (DataSnapshot i : circleData.getChildren())
            {
                double lat = (double) i.child("lat").getValue();
                double lng = (double) i.child("lng").getValue();
                int radius = ((Long) i.child("radius").getValue()).intValue();
                float[] distance = new float[2];
                Location.distanceBetween(myLocation.latitude, myLocation.longitude, lat, lng, distance);
                if (distance[0] <= radius)
                {
                    int rate = ((Long) i.child("rate").getValue()).intValue();
                    Firebase ref = i.getRef();
                    Map<String, Object> rateNode = new HashMap<>();
                    rateNode.put("rate", rate + 1);
                    ref.updateChildren(rateNode);
                    find = true;
                    break;
                }
            }
        }

        //ArrayList<Float> listDistance = new ArrayList<>();
        if (!find)
        {
            int myRadius = TrafficCircle.getRadius(radiusPicker.getProgress() / 2);
            for (DataSnapshot i : circleData.getChildren())
            {
                double lat = (double) i.child("lat").getValue();
                double lng = (double) i.child("lng").getValue();
                int radius = ((Long) i.child("radius").getValue()).intValue();
                float[] distance = new float[2];
                Location.distanceBetween(myLocation.latitude, myLocation.longitude, lat, lng, distance);
                if (distance[0] > radius + myRadius)      // no intersect --> create new circle
                {
                    Map<String, Object> circleNode = new HashMap<>();
                    circleNode.put("lat", myLocation.latitude);
                    circleNode.put("lng", myLocation.longitude);
                    circleNode.put("radius", myRadius);
                    circleNode.put("rate", 1);

                    Firebase ref = circleData.getRef();
                    ref.push().setValue(circleNode);

                    find = true;
                    break;
                }
                //listDistance.add(distance[0]);
            }

            // I have no idea what i'm gonna do with this shit
            /*if (!find)
            {
                distan
                DirectionAst asyncTask = new DirectionAst();
                asyncTask.setOnLoadListener(new OnLoadListener()
                {
                    @Override
                    public void onFinish(Object o)
                    {

                    }
                });
                asyncTask.execute(myLocation, new LatLng())
            }*/
        }
        dialog.dismiss();


        ref.removeEventListener(this);
        if (find)
        {
            String address = tvAddress.getText().toString();
            address = address.substring(12);
            MainActivity.sqlite.saveTraffic(myLocation.latitude, myLocation.longitude, TrafficCircle.getRadius(radiusPicker.getProgress()) / 2, address);
            MessageDialog messageDialog = new MessageDialog(this, R.color.lime, R.drawable.smile, "Gửi thông báo thành công", "Cảm ơn bạn đã thông báo vị trí ùn tắc giao thông cho tất cả mọi người cùng được biết");
            messageDialog.show();
        }
        else
        {
            MessageDialog messageDialog = new MessageDialog(this, R.color.colorPrimaryDark, R.drawable.error, "Chưa gửi được thông báo", "Xin hãy thử lại");
            messageDialog.show();
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError)
    {

    }
}
