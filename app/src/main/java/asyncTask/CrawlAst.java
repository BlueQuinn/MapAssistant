package asyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;

import model.Food;

/**
 * Created by lequan on 10/10/2016.
 */
public class CrawlAst extends AsyncTask<String, Integer, ArrayList<?>>
{

    ArrayList<?> crawlData(String url)
    {
        return null;
    }


    @Override
    protected ArrayList<?> doInBackground(String... params)
    {
        return crawlData(params[0]);
    }
}
