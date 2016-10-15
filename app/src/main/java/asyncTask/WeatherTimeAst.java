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
public class WeatherTimeAst extends AsyncTask<String, String, ArrayList<WeatherTime>> {

    OnLoadListener<ArrayList<WeatherTime>> listener = null;
    Context _context = null;

    public WeatherTimeAst(Context context){
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

            URL link = new URL("http://freemeteo.vn/thoi-tiet/ho-chi-minh-city/hourly-forecast/today/?gid=1566083&language=vietnamese&country=vietnam");
            HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");

            TagNode root = htmlCleaner.clean(urlConnection.getInputStream());
            Log.d("Son","root");

            Object[] nodes_time = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[3]/div/table/thead/tr/th"));
            Object[] nodes_info = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[3]/div/table/tbody/tr[2]/td"));
            Object[] nodes_temps = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[3]/div/table/tbody/tr[3]/td"));
            Object[] nodes_hum = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[3]/div/table/tbody/tr[6]/td"));
            Object[] nodes_rain = (root.evaluateXPath("//*[@id='content']/div[3]/div[2]/div[3]/div/table/tbody/tr[8]/td"));

            Log.d("Son","nodes");
            for (int i = 1; i < nodes_time.length; ++i) {
                TagNode node_time = (TagNode) nodes_time[i];
                String time = node_time.getText().toString();

                TagNode node_info = (TagNode) nodes_info[i-1];
                String js_info = node_info.getText().toString();

                String info = "unknow";
                String temp = "document.write(Icons.GetDescription(";
                if(js_info.contains(temp)) {
                    int start = js_info.indexOf(temp) + temp.length();
                    int end = js_info.indexOf(",");
                    if(end < 0)
                        end = js_info.indexOf("))");

                    int index_info = Integer.parseInt(js_info.substring(start, end));
                    // index to read json form file
                    info = ReadJSwithIndex(index_info, root_weather);
                }
                else{
                    continue;
                }

                TagNode node_rain = (TagNode) nodes_rain[i];
                String rain = node_rain.getText().toString();

                TagNode node_hum = (TagNode) nodes_hum[i];
                String hum = node_hum.getText().toString();

                TagNode node_temps = (TagNode) nodes_temps[i];
                String s_temps = node_temps.getText().toString();

                int temps = -1;
                if(s_temps.contains("°C")) {
                    s_temps = s_temps.replace("°C", "");
                    temps = Integer.parseInt(s_temps);
                }

                WeatherTime weatherTime = new WeatherTime(time, info, rain, hum, temps);
                lstData.add(weatherTime);
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

    @Override
    protected void onPostExecute(ArrayList<WeatherTime> lstData) {
        listener.onFinish(lstData);
    }


}
