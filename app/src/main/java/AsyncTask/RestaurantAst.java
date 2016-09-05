package AsyncTask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import Listener.OnLoadListener;
import Utils.UnicodeConverter;
import com.bluebirdaward.mapassistant.gmmap.R;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


import model.Restaurant;

/**
 * Created by Quan-DevTeam on 10/8/15.
 */
public class RestaurantAst extends AsyncTask<String, Integer, ArrayList<Restaurant>>
{

    ProgressBar prbLoading;

    public RestaurantAst(View view)
    {
        prbLoading = (ProgressBar) view.findViewById(R.id.prbLoading);
    }

    @Override
    protected ArrayList<Restaurant> doInBackground(String... params)
    {
        ArrayList<Restaurant> listRestaurant = new ArrayList<>();
        try
        {
            HtmlCleaner htmlCleaner = new HtmlCleaner();
            CleanerProperties cleanerProperties = htmlCleaner.getProperties();
            cleanerProperties.setAllowHtmlInsideAttributes(true);
            cleanerProperties.setAllowMultiWordAttributes(true);
            cleanerProperties.setRecognizeUnicodeChars(true);
            cleanerProperties.setOmitComments(true);

            URL link = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");

            TagNode root = htmlCleaner.clean(urlConnection.getInputStream());

            Object[] node_title = root.evaluateXPath("//img[@class='border-radius4']/@title");
            Object[] node_url = root.evaluateXPath("//a[@class='name-restaurant bold capitalize']/@href");
            Object[] nodeAddress = root.evaluateXPath("//p[@class='font14 clearfix']/span/text()");
            Object[] nodeImg = root.evaluateXPath("//img[@class='border-radius4']/@src");

            for (int i = 0; i < nodeImg.length; ++i)
            {
                String title = UnicodeConverter.convert((String) node_title[i]);
                String url = (String) node_url[i];
                String address = UnicodeConverter.convert(nodeAddress[i].toString());
                String img = (String) nodeImg[i];

                listRestaurant.add(new Restaurant(title, img, address, url));
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (XPatherException e)
        {
            e.printStackTrace();
        }
        return listRestaurant;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        prbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(ArrayList<Restaurant> album)
    {
        super.onPostExecute(album);
        listener.onFinish(album);
        prbLoading.setVisibility(View.GONE);
    }

    OnLoadListener listener;

    public void setOnLoaded(OnLoadListener onLoaded)
    {
        this.listener = onLoaded;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
    }
}
