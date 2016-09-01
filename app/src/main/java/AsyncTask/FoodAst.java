package AsyncTask;

import android.os.AsyncTask;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import DTO.Food;
import Listener.OnLoadListener;
import Utils.UnicodeConverter;

/**
 * Created by Quan-DevTeam on 10/8/15.
 */
public class FoodAst extends AsyncTask<String, Integer, ArrayList<Food>>
{

    public FoodAst()
    {

    }

    @Override
    protected ArrayList<Food> doInBackground(String... params)
    {
        ArrayList<Food> list = new ArrayList<>();
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

            Object[] node_title = root.evaluateXPath("//img[@class='materialboxed']/@title");
            Object[] node_url = root.evaluateXPath("//span[@class='txt-blue font16 bold']/text()");
            Object[] nodeImg = root.evaluateXPath("//img[@class='materialboxed']/@src");

            for (int i = 0; i < nodeImg.length; ++i)
            {
                String title = UnicodeConverter.convert((String) node_title[i]);
                String url = node_url[i].toString();
                String img = (String) nodeImg[i];
                img = img.replace("/content/images/no-image.png", "");

                list.add(new Food(title, url, img));
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
        return list;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Food> album)
    {
        super.onPostExecute(album);
        listener.onFinish(album);
    }

    OnLoadListener<ArrayList<Food>> listener;

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
