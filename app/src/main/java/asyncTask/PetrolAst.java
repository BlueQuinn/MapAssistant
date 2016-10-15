package asyncTask;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;


import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import listener.OnLoadListener;
import model.Petrol;
import model.WeatherWeek;

/**
 * Created by SonPham on 10/14/2016.
 */
public class PetrolAst  extends AsyncTask<String, String, JSONObject> {

    OnLoadListener<JSONObject> listener = null;

    public void setListener(OnLoadListener<JSONObject> listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject data = new JSONObject();
        try {
            HtmlCleaner htmlCleaner = new HtmlCleaner();
            CleanerProperties cleanerProperties = htmlCleaner.getProperties();
            cleanerProperties.setAllowHtmlInsideAttributes(true);
            cleanerProperties.setAllowMultiWordAttributes(true);
            cleanerProperties.setRecognizeUnicodeChars(true);
            cleanerProperties.setOmitComments(true);

            URL link = new URL("http://webgia.com/gia-xang-dau/");
            HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");

            TagNode root = htmlCleaner.clean(urlConnection.getInputStream());
            Log.d("Son", "root");
            Object[] nodes_time = (root.evaluateXPath("//*[@class='section-head']/small"));
            String time = ((TagNode)nodes_time[0]).getText().toString();
            time.replace("- ","");
            data.put("time",time);

            Object[] nodes_name = (root.evaluateXPath("//*[@id='tc_gia_vang']/div/div/table/tbody/tr/th"));
            Object[] nodes_price = (root.evaluateXPath("//*[@id='tc_gia_vang']/div/div/table/tbody/tr/td[1]"));

            Log.d("Son","nodes");
            ArrayList<Petrol> lstPetrol = new ArrayList<>();
            for (int i = 0; i < nodes_name.length; ++i) {
                TagNode node_name = (TagNode)nodes_name[i];
                String name = node_name.getText().toString();

                TagNode node_price = (TagNode)nodes_price[i];
                String price = node_price.getText().toString();

                Petrol petrol = new Petrol(name,price);
                lstPetrol.add(petrol);
            }
            data.put("ListPetrol",lstPetrol);

        } catch (IOException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        } catch (XPatherException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Son", "finish");
        return data;
    }

    @Override
    protected void onPostExecute(JSONObject data) {
        listener.onFinish(data);
    }
}

