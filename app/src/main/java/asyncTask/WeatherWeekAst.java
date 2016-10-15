package asyncTask;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import listener.OnLoadListener;
import model.WeatherWeek;

/**
 * Created by SonPham on 10/13/2016.
 */
public class WeatherWeekAst extends AsyncTask<String, String, ArrayList<WeatherWeek>> {

    OnLoadListener<ArrayList<WeatherWeek>> listener = null;

    public void setListener(OnLoadListener<ArrayList<WeatherWeek>> listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected ArrayList<WeatherWeek> doInBackground(String... params) {
        ArrayList<WeatherWeek> lstData = new ArrayList<>();
        try {
            HtmlCleaner htmlCleaner = new HtmlCleaner();
            CleanerProperties cleanerProperties = htmlCleaner.getProperties();
            cleanerProperties.setAllowHtmlInsideAttributes(true);
            cleanerProperties.setAllowMultiWordAttributes(true);
            cleanerProperties.setRecognizeUnicodeChars(true);
            cleanerProperties.setOmitComments(true);

            URL link = new URL("http://freemeteo.vn/thoi-tiet/ho-chi-minh-city/7-days/list/?gid=1566083&language=vietnamese&country=vietnam");
            HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");

            TagNode root = htmlCleaner.clean(urlConnection.getInputStream());
            Log.d("Son","root");
            Object[] nodes_weather = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[2]/div/div/div"));

            Log.d("Son","nodes");
            for (int i = 0; i < nodes_weather.length; ++i) {
                TagNode node =  (TagNode)nodes_weather[i];
                WeatherWeek item = parse2WeatherWeek(node);
                lstData.add(item);
            }

        } catch (IOException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        } catch (XPatherException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        }
        Log.d("Son", "finish");
        return lstData;
    }

    private WeatherWeek parse2WeatherWeek(TagNode node) {
        WeatherWeek weatherWeek = null;
        try {
            Object[] node_date = (node.evaluateXPath("//*[@class='title']"));
            Object[] node_info = (node.evaluateXPath("//*[@class='info']"));
            Object[] node_rain = (node.evaluateXPath("//*[@class='extra']"));
            Object[] node_temp = (node.evaluateXPath("//*[@class='temps']"));

            String date = ((TagNode)node_date[0]).getText().toString();
            String info = ((TagNode)node_info[0]).getText().toString();
            String rain = ((TagNode)node_rain[0]).getText().toString();

            String temps = ((TagNode)node_temp[0]).getText().toString();
            int min = -1;
            int max = -1;
            String max_name = "tối đa: ";
            String min_name = "tối thiểu: ";

            if(temps.contains(max_name)){
                int start = temps.indexOf(max_name) + max_name.length();
                int end = temps.indexOf("°C");
                String s_max = temps.substring(start,end);
                max = Integer.parseInt(s_max);
            }

            if(temps.contains(min_name)){
                int start = temps.indexOf(min_name) + min_name.length();
                int end = temps.lastIndexOf("°C");
                String s_max = temps.substring(start,end);
                min = Integer.parseInt(s_max);
            }

            weatherWeek = new WeatherWeek(date,info,rain,min,max);
        } catch (XPatherException e) {
            e.printStackTrace();
        }


        return weatherWeek;
    }

    @Override
    protected void onPostExecute(ArrayList<WeatherWeek> lstData) {
        listener.onFinish(lstData);
    }
}
