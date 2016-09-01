package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;
import DTO.Nearby;
import Listener.OnLoadListener;

/**
 * Created by lequan on 8/30/2016.
 */
public class PlacePickerDialog extends Dialog
{
    public void setOnPickListener(OnLoadListener<Nearby> listener)
    {
        this.listener = listener;
    }

    OnLoadListener<Nearby> listener;

    public PlacePickerDialog(Context context)
    {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_place_picker);

        final TextView txtRadius = (TextView) findViewById(R.id.txtRadius);
        final SeekBar radiusPicker = (SeekBar) findViewById(R.id.radiusPicker);
        radiusPicker.setProgress(2);    // default 3km
        radiusPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                txtRadius.setText("Phạm vi tìm kiếm " + Integer.toString(progress + 1) + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        AutoCompleteTextView autoCompleteSearch = (AutoCompleteTextView) findViewById(R.id.auto_complete_search);

        String[] places = {"Accounting", "Airport", "Amusement Park", "Aquarium", "Art Gallery", "Atm", "Bakery", "Bank", "Bar", "Beauty Salon", "Bicycle Store", "Book Store", "Bus Station", "Cafe", "Campground", "Car Dealer", "Car Rental", "Car Repair", "Car Wash", "Casino", "Cemetery", "Church", "City Hall", "Clothing Store", "Convenience Store", "Courthouse", "Dentist", "Department Store", "Doctor", "Electrician", "Electronics Store", "Embassy", "Finance", "Fire Station", "Florist", "Food", "Funeral Home", "Furniture Store", "Gas Station", "Grocery Or Supermarket", "Gym", "Hair Care", "Hardware Store", "Health", "Home Goods Store", "Hospital", "Insurance Agency", "Jewelry Store", "Laundry", "Lawyer", "Library", "Liquor Store", "Local Government Office", "Locksmith", "Lodging", "Meal Delivery", "Meal Takeaway", "Mosque", "Movie Rental", "Movie Theater", "Moving Company", "Museum", "Night Club", "Painter", "Park", "Parking", "Pet Store", "Pharmacy", "Plumber", "Police", "Post Office", "Real Estate Agency", "Restaurant", "School", "Shoe Store", "Shopping Mall", "Spa", "Stadium", "Storage", "Store", "Taxi Stand", "Train Station", "Travel Agency", "University", "Zoo"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, places);
        autoCompleteSearch.setAdapter(adapter);
        autoCompleteSearch.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String placeType = adapter.getItem(position).toLowerCase().replace(" ", "_");
                listener.onFinish(new Nearby(placeType, radiusPicker.getProgress() + 1));
            }
        });
    }
}
