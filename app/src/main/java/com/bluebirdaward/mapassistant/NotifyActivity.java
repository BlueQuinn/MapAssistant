package com.bluebirdaward.mapassistant;

import android.content.DialogInterface;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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

import DTO.Jam;
import DTO.Position;
import Utils.AddressUtils;
import com.bluebirdaward.mapassistant.gmmap.R;

public class NotifyActivity extends AppCompatActivity implements View.OnClickListener
{
    boolean change;
    double myLat;
    double myLng;
    TextView tvPlace, tvAddress;
    Button btnNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        Firebase.setAndroidContext(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myLat = MainActivity.myLocation.latitude;
        myLng = MainActivity.myLocation.longitude;

        tvPlace = (TextView) findViewById(R.id.tvPlace);
        tvAddress = (TextView) findViewById(R.id.tvAddress);

        btnNotify = (Button) findViewById(R.id.btnNotify);

        String address = AddressUtils.getAddress(new Geocoder(this, Locale.getDefault()), myLat, myLng);
        if (address.length() < 1)
        {
            tvAddress.setText("Không có kết nối internet");
            btnNotify.setEnabled(false);
            return;
        }

        tvAddress.setText("Bạn đang ở " + address);
        btnNotify.setOnClickListener(this);
    }

    float getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        Location homeLocation = new Location("");
        homeLocation.setLatitude(lat1);
        homeLocation.setLongitude(lng1);

        Location targetLocation = new android.location.Location("");
        targetLocation.setLatitude(lat2);
        targetLocation.setLongitude(lng2);

        return 1000 * targetLocation.distanceTo(homeLocation);
    }

    int toMinutes(String time)
    {
        String[] hourMin = time.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int mins = Integer.parseInt(hourMin[1]);
        int hoursInMins = hour * 60;
        return hoursInMins + mins;
    }

    @Override
    public void onClick(View v)
    {
        if (checkLocation())
        {
            showMessage("Không thể gửi thông báo !!!\nBạn đã từng thông báo vị trí này từ trước đó rồi");
        }
        else
        {
            saveFirebase();
        }
    }

    boolean checkLocation()
    {
        ArrayList<HashMap<String, String>> listRow = MainActivity.dbHelper.getAll("Location");
        for (HashMap<String, String> row : listRow)
        {
            double lat = Double.parseDouble(row.get("Lat"));
            double lng = Double.parseDouble(row.get("Lng"));
            float distance = getDistance(myLat, myLng, lat, lng);
            Log.d("123", "lat = " + lat + " " + myLat);
            Log.d("123", "lng = " + lng + " " + myLng);
            Log.d("123", "distance = " + distance);
            if (distance >= 0 && distance <= 1)
            {
                return true;
            }
        }
        return false;
    }

    void saveFirebase()
    {
        change = false;

        final Firebase ref = new Firebase(getResources().getString(R.string.trafficDatabase));
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                if (!change)
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date date = new Date();
                    int timeNow = 60 * date.getHours() + date.getMinutes();

                    boolean findPosition = false;
                    int meta = ((Long) snapshot.child("meta").getValue()).intValue();
                    DataSnapshot traffic = snapshot.child("traffic");
                    for (DataSnapshot item : traffic.getChildren())
                    {
                        double lat = (Double) item.child("position/lat").getValue();
                        double lng = (Double) item.child("position/lng").getValue();
                        float distance = getDistance(myLat, myLng, lat, lng);
                        if (distance >= 0 && distance <= 1)     // kiểm tra xem trên Firebase đã tồn tại điểm nào ở gần điểm này mà bị tắc đường chưa (gần ở đây là dưới 1km)
                        {
                            boolean findTime = false;
                            DataSnapshot jamList = item.child("jam");
                            for (DataSnapshot jamItem : jamList.getChildren())
                            {
                                String time = (String) jamItem.child("time").getValue();
                                int timeSpan = timeNow - toMinutes(time);
                                if (timeSpan > -90 || timeSpan < 90)
                                {
                                    int vote = ((Long) jamItem.child("vote").getValue()).intValue();
                                    Firebase jamRef = jamItem.getRef();
                                    Map<String, Object> voteMap = new HashMap<>();
                                    voteMap.put("vote", vote + 1);
                                    jamRef.updateChildren(voteMap);
                                    findTime = true;
                                    change = true;
                                    break;
                                }
                            }
                            if (!findTime)
                            {
                                Firebase trafficRef = item.getRef();
                                Map<String, Object> jam = new HashMap<>();
                                jam.put("time", formatter.format(date));
                                jam.put("vote", 1);
                                trafficRef.push().setValue(jam);
                                change = true;
                            }
                            findPosition = true;
                            break;
                        }
                    }
                    if (!findPosition)
                    {
                        Firebase trafficRef = traffic.getRef();
                        Position position = new Position(myLat, myLng);
                        Jam[] jam = new Jam[]{new Jam(formatter.format(date), 1)};
                        Map<String, Object> trafficItem = new HashMap<>();
                        trafficItem.put("position", position);
                        trafficItem.put("jam", jam);
                        trafficRef.push().setValue(trafficItem);
                        change = true;
                    }
                }
                else
                {
                    MainActivity.dbHelper.saveLocation(myLat, myLng);
                    showMessage("Thông báo của bạn đã được gửi đi");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    void showMessage(String message)
    {
        new AlertDialog.Builder(this).setMessage(message).show()
                .setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        finish();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
