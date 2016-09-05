package Fragment;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import Listener.DestinationListener;

import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 4/19/2016.
 */
public class PlacePickerFragment extends Fragment implements OnClickListener
{

    ImageButton btnBack;

    private TextView textView;
    @Nullable
    private LatLngBounds bounds;
    @Nullable
    private AutocompleteFilter filter;
    @Nullable
    private PlaceSelectionListener placeListener;

    DestinationListener listener;

    public void setOnCloseListener(DestinationListener listener)
    {
        this.listener = listener;
    }

    public PlacePickerFragment()
    {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View var4 = inflater.inflate(R.layout.fragment_place_picker, container, false);
        btnBack = (ImageButton) var4.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        //zzaRi = var4.findViewById(R.id.imvClose);
        textView = (TextView) var4.findViewById(R.id.txtSearch);
        OnClickListener var5 = new OnClickListener()
        {
            public void onClick(View view)
            {
                zzzG();
            }
        };
        textView.setOnClickListener(var5);
        return var4;
    }

    public void onDestroyView()
    {
        btnBack = null;
        //zzaRi = null;
        textView = null;
        super.onDestroyView();
    }

    public void setBoundsBias(@Nullable LatLngBounds bounds)
    {
        this.bounds = bounds;
    }

    public void setFilter(@Nullable AutocompleteFilter filter)
    {
        this.filter = filter;
    }

    public void findPlace(CharSequence text)
    {
        textView.setText(text);
        if (text.length() < 1)
        {

        }
        zzzG();
    }

    public void setText(String text)
    {
        textView.setText(text);
    }

    public void setHint(CharSequence hint)
    {
        textView.setHint(hint);
        btnBack.setContentDescription(hint);
    }

    public void setOnPlaceSelectedListener(PlaceSelectionListener listener)
    {
        placeListener = listener;
    }

    void zzzG()
    {
        int var1 = -1;
        try
        {
            Intent var2 = (new PlaceAutocomplete.IntentBuilder(2)).setBoundsBias(bounds).setFilter(filter).zzeq(textView.getText().toString()).zzig(1).build(getActivity());
            startActivityForResult(var2, 1);
        }
        catch (GooglePlayServicesRepairableException var3)
        {
            var1 = var3.getConnectionStatusCode();
            Log.e("Places", "Could not open autocomplete activity", var3);
        }
        catch (GooglePlayServicesNotAvailableException var4)
        {
            var1 = var4.errorCode;
            Log.e("Places", "Could not open autocomplete activity", var4);
        }

        if (var1 != -1)
        {
            GoogleApiAvailability var5 = GoogleApiAvailability.getInstance();
            var5.showErrorDialogFragment(getActivity(), var1, 2);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)   // find place, address
        {
            if (resultCode == -1)
            {
                Place var4 = PlaceAutocomplete.getPlace(getActivity(), data);
                if (placeListener != null)
                {
                    placeListener.onPlaceSelected(var4);
                }
                textView.setText(var4.getName().toString());
            }
            else if (resultCode == 2)
            {
                Status var5 = PlaceAutocomplete.getStatus(getActivity(), data);
                if (placeListener != null)
                {
                    placeListener.onError(var5);
                }
            }
        }
        else    // find nearby
        {
            if (resultCode == -1)
            {
                Place place = PlacePicker.getPlace(data, getActivity());
                placeListener.onPlaceSelected(place);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean remove = false;

    public void setRemove(boolean remove)
    {
        this.remove = remove;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnBack)
        {
            if (remove)
            {
                listener.disableRemove();
            }
            else
            {
                listener.onClose();
            }
        }
       /* else
        {
            listener.onRemove();
        }*/
    }

    LatLngBounds toBounds(LatLng center, double radius)
    {
        LatLng southwest = computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    static LatLng computeOffset(LatLng from, double distance, double heading)
    {
        distance /= 6371009.0D;     // Earth's radius
        heading = Math.toRadians(heading);
        double fromLat = Math.toRadians(from.latitude);
        double fromLng = Math.toRadians(from.longitude);
        double cosDistance = Math.cos(distance);
        double sinDistance = Math.sin(distance);
        double sinFromLat = Math.sin(fromLat);
        double cosFromLat = Math.cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading);
        double dLng = Math.atan2(sinDistance * cosFromLat * Math.sin(heading), cosDistance - sinFromLat * sinLat);
        return new LatLng(Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(fromLng + dLng));
    }
}
