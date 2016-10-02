package asyncTask;

import android.os.AsyncTask;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Jam;
import listener.OnLoadListener;

/**
 * Created by lequan on 5/13/2016.
 */
public class TrafficAst extends AsyncTask<String, Integer, ArrayList<LatLng>>
{
    OnLoadListener listener;
    ArrayList<LatLng> list;

    public void setOnLoadListener(OnLoadListener listener)
    {
        this.listener = listener;
    }

    public TrafficAst()
    {
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> result)
    {
        listener.onFinish(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected ArrayList<LatLng> doInBackground(String... params)
    {
        list = new ArrayList<>();
        Firebase ref = new Firebase("https://androidtraffic.firebaseio.com/");
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                try
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date date = new Date();
                    int timeNow = 60 * date.getHours() + date.getMinutes();

                    int meta = ((Long) snapshot.child("meta").getValue()).intValue();
                    DataSnapshot traffic = snapshot.child("traffic");
                    for (DataSnapshot item : traffic.getChildren())
                    {
                        ArrayList<Jam> jamList = new ArrayList<>();
                        DataSnapshot jamData = item.child("jam");
                        for (DataSnapshot jam : jamData.getChildren())
                        {
                            String time = (String) jam.child("time").getValue();
                            Date jamTime = formatter.parse(time);
                            int span = timeNow - 60 * jamTime.getHours() - jamTime.getMinutes();
                            if (span > -90 && span < 90)   // timespan between 90 minutes earlier or later
                            {
                                int vote = ((Integer) jam.child("voteList").getValue()).intValue();
                                if (vote > meta)
                                {
                                    jamList.add(new Jam(time, vote));
                                    break;
                                }
                            }
                        }

                        if (jamList.size() > 0)
                        {
                            //sortDescending(jamList);
                            double lat = (double) item.child("position/lat").getValue();
                            double lng = (double) item.child("position/lng").getValue();
                            list.add(new LatLng(lat, lng));
                            //Traffic traffic = new Traffic(lat, lng, jamList.get(0).getVote());
                        }
                    }
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
            }
        });
        return list;
    }

}
