package fragment;

import asyncTask.DestinationAst;
import com.bluebirdaward.mapassistant.MainActivity;

/**
 * Created by lequan on 4/28/2016.
 */
public class HistoryFragment extends DestinationFragment
{
    public HistoryFragment()
    {

    }

    @Override
    void initAsyncTask()
    {
        asyncTask = new DestinationAst("History");
    }

    public void save(String place, String address, double lat, double lng)
    {
        MainActivity.dbHelper.delete("History", place);
        MainActivity.dbHelper.insert("History", place, address, lat, lng);
    }

    /*@Override
    void onSelected(int position)
    {

        //listener.onSelected(list.get(position).getName());
        double[] location = MainActivity.dbHelper.getPlace("History", list.get(position).getName());
        double lat = location[0];
        double lng = location[1];

    }*/
}
