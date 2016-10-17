package com.bluebirdaward.mapassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import asyncTask.AddressAst;
import listener.OnLoadListener;
import model.Traffic;
import utils.MapUtils;
import utils.PolyUtils;
import utils.RequestCode;
import utils.TrafficUtils;
import widgets.MessageDialog;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.model.LatLng;

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
    Firebase firebase;
    Query query;
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
                    if (isOnline())
                    saveTraffic();
                    else
                        Toast.makeText(NotifyActivity.this, "Không có kết nối Internet", Toast.LENGTH_SHORT).show();
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

                   /* new ShowcaseView.Builder(NotifyActivity.this)
                            .setTarget(new ViewTarget(R.id.btnNotify, NotifyActivity.this))
                            .setContentTitle("ShowcaseView")
                            .setContentText("This is highlighting the Home button")
                            .hideOnTouchOutside()
                            .build();*/
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
            if (distance < getRadius() + radius)
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        tvRadius.setText("Ước tính phạm vi ùn tắc: " + getLength() + "m");
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

    int time;

    void saveTraffic()
    {
        dialog = new MessageDialog(this);
        dialog.show();

        time = TrafficUtils.trafficTime();

        firebase = new Firebase(getResources().getString(R.string.database_traffic));
        query = firebase.child("line").orderByChild("time").startAt(time - 30).endAt(time + 30);
        query.addListenerForSingleValueEvent(this);
    }

    String jamType = Traffic.MY_TRAFFIC;
    int id = -1;
    boolean intersect;

    boolean checkLine(DataSnapshot snapshot)
    {
        for (DataSnapshot i : snapshot.getChildren())       // put to existing line
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
                jamType = Traffic.LINE;
                return true;
            }
        }
        return false;
    }

    boolean checkCircle(DataSnapshot dataSnapshot)
    {
        for (DataSnapshot i : dataSnapshot.getChildren())
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
                jamType = Traffic.CIRCLE;
                return true;
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
        return false;
    }


    @Override
    public void onDataChange(DataSnapshot lineData)
    {
        if (!checkLine(lineData))
        {
            intersect = false;
            query = firebase.child("circle").orderByChild("time").startAt(time - 30).endAt(time + 30);
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot circleData)
                {
                    if (!checkCircle(circleData))       // intersect will be change in checkCircle(), in a distant future ?
                    {
                        if (intersect)     // is intersect = true
                        {

                        }
                        else    // no intersect, create new circle --> my traffic
                        {
                            id = (int) circleData.getChildrenCount();       // circleData always has children
                            Map<String, Object> circleNode = new HashMap<>();
                            circleNode.put("lat", myLocation.latitude);
                            circleNode.put("lng", myLocation.longitude);
                            circleNode.put("time", time);
                            circleNode.put("radius", getRadius());
                            circleNode.put("rate", 1);
                            circleNode.put("id", id);

                            Firebase ref = circleData.getRef();
                            ref.push().setValue(circleNode);
                            jamType = Traffic.MY_TRAFFIC;

                            submit();
                        }
                    }
                    else
                    {
                        submit();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError)
                {

                }
            });
        }
        else
        {
            submit();
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError)
    {

    }

    void submit()
    {
        firebase.removeEventListener(this);
        if (id > -1)
        {
            String address = tvAddress.getText().toString().replace("Bạn đang ở ", "");
            MainActivity.sqlite.saveTraffic(id, myLocation.latitude, myLocation.longitude, getRadius(), address, jamType);
            dialog.show(getResources().getColor(R.color.green), R.drawable.smile, "Gửi thông báo thành công", "Cảm ơn bạn đã thông báo vị trí ùn tắc giao thông này cho tất cả mọi người cùng được biết");
        }
        else
        {
            dialog.show(getResources().getColor(R.color.colorPrimary), R.drawable.error, "Chưa gửi được thông báo", "Xin hãy thử lại");
        }
    }


    int getLength()
    {
        return (radiusPicker.getProgress() + 4) * 50;
    }

    int getRadius()
    {
        return getLength() / 2;
    }

    boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
