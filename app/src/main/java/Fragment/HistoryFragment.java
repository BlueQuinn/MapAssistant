package Fragment;

import AsyncTask.DestinationAst;
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

    public void save(String place, String address)
    {
        MainActivity.dbHelper.delete("History", place);
        MainActivity.dbHelper.insert("History", place, address);
    }

    @Override
    void onSelected(int position)
    {
        listener.onSelected(list.get(position).getName());
    }

    @Override
    String fragmentName()
    {
        return "History";
    }
}
