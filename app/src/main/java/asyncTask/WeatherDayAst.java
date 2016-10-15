package asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import listener.OnLoadListener;
import model.WeatherTime;

/**
 * Created by SonPham on 10/13/2016.
 */
public class WeatherDayAst extends AsyncTask<String, String, ArrayList<WeatherTime>> {

    OnLoadListener<ArrayList<WeatherTime>> listener = null;
    Context _context = null;

    public WeatherDayAst(Context context){
        this._context = context;
    }


    public void setListener(OnLoadListener<ArrayList<WeatherTime>> listener) {
        this.listener =listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected ArrayList<WeatherTime> doInBackground(String... params) {
        ArrayList<WeatherTime> lstData = new ArrayList<>();

        try {
            String data = loadJSONFromAsset();
            JSONObject root_weather = new JSONObject(data);

            HtmlCleaner htmlCleaner = new HtmlCleaner();
            CleanerProperties cleanerProperties = htmlCleaner.getProperties();
            cleanerProperties.setAllowHtmlInsideAttributes(true);
            cleanerProperties.setAllowMultiWordAttributes(true);
            cleanerProperties.setRecognizeUnicodeChars(true);
            cleanerProperties.setOmitComments(true);

            URL link = new URL("http://freemeteo.vn/thoi-tiet/ho-chi-minh-city/daily-forecast/today/?gid=1566083&language=vietnamese&country=vietnam");
            HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");

            TagNode root = htmlCleaner.clean(urlConnection.getInputStream());
            Log.d("Son","root");

            Object[] nodes_day = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[3]/a"));

            Log.d("Son","nodes");
            for (int i = 0; i < nodes_day.length - 1; ++i) {
                TagNode node_day = (TagNode)nodes_day[i];
                WeatherTime weatherTime = parse2WeatherTime(node_day);
                if(weatherTime!=null){
                    lstData.add(weatherTime);
                }

            }

        } catch (IOException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        } catch (XPatherException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("Son", "error " + e.toString());
            e.printStackTrace();
        }
        Log.d("Son", "finish");
        return lstData;
    }

    private WeatherTime parse2WeatherTime(TagNode node) {
        WeatherTime weatherTime = null;
        try {

            String data = loadJSONFromAsset();
            JSONObject root_weather = new JSONObject(data);

            Object[] node_time = (node.evaluateXPath("//*[@class='title']"));
            Object[] node_info = (node.evaluateXPath("/span[2]"));
            Object[] node_description = (node.evaluateXPath("//*[@class='info']/strong"));
            Object[] node_temp = (node.evaluateXPath("//*[@class='temp']"));

            String time = ((TagNode)node_time[0]).getText().toString();

            String js_info = ((TagNode)node_info[0]).getText().toString();
            String info = "unknow";
            String temp = "document.write(Icons.GetDescription(";
            if(js_info.contains(temp)) {
                int start = js_info.indexOf(temp) + temp.length();
                int end = js_info.indexOf("))");
                int index_info = Integer.parseInt(js_info.substring(start, end));
                // index to read json form file
                info = ReadJSwithIndex(index_info, root_weather);
            }
            else {
                return null;
            }



            String rain = ((TagNode)node_description[2]).getText().toString();

            String hum = ((TagNode)node_description[0]).getText().toString()
                    + ((TagNode)node_description[1]).getText().toString();
            hum = hum.replace("\r\n","");
            hum = hum.replace("  ","");


            String s_temps = ((TagNode)node_temp[0]).getText().toString();
            int temps = -1;
            if(s_temps.contains("°C")) {
                s_temps = s_temps.replace("°C", "");
                temps = Integer.parseInt(s_temps);
            }




            weatherTime = new WeatherTime(time,info,rain,hum,temps);
        } catch (XPatherException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return weatherTime;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = _context.getAssets().open("weather.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    private String ReadJSwithIndex(int index_info,JSONObject root_weather) {
        String info = "unknow";
        try {

            JSONObject item = root_weather.getJSONObject("Forecast").getJSONObject("" + index_info);
            info = item.get("Description").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    @Override
    protected void onPostExecute(ArrayList<WeatherTime> lstData) {
        listener.onFinish(lstData);
    }


}
