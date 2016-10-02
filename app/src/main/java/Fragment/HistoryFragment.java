package fragment;

import com.bluebirdaward.mapassistant.MainActivity;

/**
 * Created by lequan on 4/28/2016.
 */
public class HistoryFragment extends DestinationFragment
{
    public HistoryFragment()
    {
        fragmentName = "History";
    }

    public void save(String place, String address, double lat, double lng)
    {
        MainActivity.dbHelper.delete("History", place);
        MainActivity.dbHelper.insert("History", place, address, lat, lng);
    }

}
