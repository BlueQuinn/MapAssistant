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
import model.Traffic;
import model.TrafficCircle;
import utils.MapUtils;
import utils.PolyUtils;
import utils.RequestCode;
import utils.TrafficUtils;
import widgets.LoadingDialog;
import widgets.MessageDialog;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.maps.model.LatLng;

import static model.TrafficCircle.getRadius;
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
    Firebase ref;
    MessageDialog dialog;

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
        // myLocation = new LatLng(10.769914, 106.670608);

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
                    MessageDialog.showMessage(NotifyActivity.this, getResources().getColor(R.color.colorPrimary), R.drawable.error, "Không thể gửi thông báo", "Bạn đã từng thông báo ở gần vị trí này từ trước đó rồi");
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
                    tvAddress.setText("Chưa có kết nối internet");
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

    boolean checkLocation()
    {
        ArrayList<HashMap<String, String>> listRow = MainActivity.sqlite.getAll("MyTraffic");
        for (HashMap<String, String> row : listRow)
        {
            double lat = Double.parseDouble(row.get("Lat"));
            double lng = Double.parseDouble(row.get("Lng"));
            int radius = Integer.parseInt(row.get("Radius"));
            float distance = MapUtils.distance(new LatLng(myLocation.latitude, myLocation.longitude), new LatLng(lat, lng));
            if (distance < getRadius(radiusPicker.getProgress() / 2) + getRadius(radius))
            {
                return true;
            }
        }
        return false;
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
        tvRadius.setText("Ước tính phạm vi ùn tắc: " + getRadius(progress) + "m");
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

    String time;

    void saveTraffic()
    {
        dialog = new MessageDialog(this);
        dialog.show();

        time = TrafficUtils.getTimeNode();
        ref = new Firebase(getResources().getString(R.string.database_traffic)).child(time);
        ref.addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot snapshot)
    {
        boolean find = false;
        String jamType = Traffic.MY_TRAFFIC;
        int id = -1;
        DataSnapshot lineData = snapshot.child("line");
        for (DataSnapshot i : lineData.getChildren())       // put to existing line
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
                id = ((Long) i.child("id").getValue()).intValue();
                find = true;
                jamType = Traffic.LINE;
                break;
            }
        }

        boolean intersect = false;
        int myRadius = getRadius(radiusPicker.getProgress()) / 2;
        DataSnapshot circleData = snapshot.child("circle");
        if (!find)      // put to existing circle
        {
            for (DataSnapshot i : circleData.getChildren())
            {
                double lat = (double) i.child("lat").getValue();
                double lng = (double) i.child("lng").getValue();
                int radius = ((Long) i.child("radius").getValue()).intValue();
                float[] distance = new float[2];
                Location.distanceBetween(myLocation.latitude, myLocation.longitude, lat, lng, distance);
                if (distance[0] <= radius)      // in circle
                {
                    int rate = ((Long) i.child("rate").getValue()).intValue();
                    Firebase ref = i.getRef();
                    Map<String, Object> rateNode = new HashMap<>();
                    rateNode.put("rate", rate + 1);
                    ref.updateChildren(rateNode);
                    id = ((Long) i.child("id").getValue()).intValue();
                    find = true;
                    jamType = Traffic.CIRCLE;
                    break;
                }
                /*else        // out of circle
                {
                    if (!intersect)
                    {
                        if (distance[0] < radius + myRadius)      // check intersect
                        {
                            intersect = true;
                        }
                    }
                }*/
            }
        }

        if (!find)
        {
            if (intersect)     // is intersect = true
            {

            }
            else    // no intersect, create new circle --> my traffic
            {
                id = (int) circleData.getChildrenCount();
                Map<String, Object> circleNode = new HashMap<>();
                circleNode.put("lat", myLocation.latitude);
                circleNode.put("lng", myLocation.longitude);
                circleNode.put("radius", radiusPicker.getProgress() / 2);
                circleNode.put("rate", 1);
                circleNode.put("id", id);

                Firebase ref = circleData.getRef();
                ref.push().setValue(circleNode);
                find = true;
                jamType = Traffic.MY_TRAFFIC;
            }
        }
        // chưa có new line

        ref.removeEventListener(this);
        if (find && id > -1)
        {
            String address = tvAddress.getText().toString().replace("Bạn đang ở ", "");
            MainActivity.sqlite.saveTraffic(id, myLocation.latitude, myLocation.longitude, radiusPicker.getProgress() / 2, address, Integer.parseInt(time), jamType);
            dialog.show(getResources().getColor(R.color.green), R.drawable.smile, "Gửi thông báo thành công", "Cảm ơn bạn đã thông báo vị trí ùn tắc giao thông này cho tất cả mọi người cùng được biết");


            Log.d("traffic", "notift " + time + " " + jamType + " " + myRadius);
        }
        else
        {
            dialog.show(getResources().getColor(R.color.colorPrimary), R.drawable.error, "Chưa gửi được thông báo", "Xin hãy thử lại");
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError)
    {

    }
}
