package asyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import model.Destination;
import listener.OnLoadListener;
import com.bluebirdaward.mapassistant.MainActivity;

/**
 * Created by lequan on 4/22/2016.
 */
public class DestinationAst extends AsyncTask<Void, Integer, ArrayList<Destination>>
{
    String table;
    OnLoadListener listener;

    public void setOnLoadListener(OnLoadListener listener)
    {
        this.listener = listener;
    }


    public DestinationAst(){}

    public DestinationAst(String table)
    {
        this.table = table;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Destination> result)
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
    protected ArrayList<Destination> doInBackground(Void... params)
    {
        return loadDestination();
    }

    ArrayList<Destination> loadDestination()
    {
        ArrayList<Destination> list = new ArrayList<>();
        ArrayList<HashMap<String, String>> listRow = MainActivity.sqlite.getAll(table);
        for (HashMap<String, String> row : listRow)
        {
            list.add(new Destination(row.get("Place"), row.get("Address")));
        }
        return list;
    }
}
