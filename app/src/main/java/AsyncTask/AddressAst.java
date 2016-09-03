package AsyncTask;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;

import Listener.OnLoadListener;
import Utils.AddressUtils;

/**
 * Created by lequan on 9/2/2016.
 */
public class AddressAst extends AsyncTask<Double, Void, String>
{
    OnLoadListener<String> listener;
    Geocoder geocoder;

    public AddressAst(Geocoder geocoder)
    {
        this.geocoder = geocoder;
    }

    @Override
    protected void onPostExecute(String address)
    {
        listener.onFinish(address);
        super.onPostExecute(address);
    }

    public void setListener(OnLoadListener<String> listener)
    {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Double... params)
    {
        String currentAddress = "";
        try
        {
            Address address = geocoder.getFromLocation(params[0], params[1], 1).get(0);
            for (int i = 0; i < address.getMaxAddressLineIndex() - 1; i++)
            {
                currentAddress += address.getAddressLine(i) + ", ";
            }
            currentAddress += address.getAddressLine(address.getMaxAddressLineIndex() - 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return currentAddress;
        //return AddressUtils.getAddress(geocoder, params[0], params[1]);
    }
}
