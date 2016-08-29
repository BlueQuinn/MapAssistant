package Utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lequan on 5/13/2016.
 */
public class JsonUtils
{
    public static JSONObject getJSON(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            InputStream stream = urlConnection.getInputStream();
            return JsonParser(stream);
        }
        catch (Exception e)
        {
            Log.e("123", e.getMessage());
        }

        return null;
    }

    public static JSONObject JsonParser(InputStream stream) throws IOException, JSONException
    {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = streamReader.readLine()) != null)
            builder.append(line);
        return new JSONObject(builder.toString());
    }
}
