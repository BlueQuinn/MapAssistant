package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import DTO.Destination;
import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 4/23/2016.
 */
public class DestinationAdt extends ArrayAdapter<Destination>
{
    ArrayList<Destination> list;
    Context context;
    int resource;
    CheckBox checkBox;

    public DestinationAdt(Context context, int resource, ArrayList<Destination> listDestination)
    {
        super(context, resource);

        list = listDestination;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);

        checkBox = (CheckBox) convertView.findViewById(R.id.chbRemove);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);

        tvAddress.setText(list.get(position).getAddress());
        tvName.setText(list.get(position).getName());

        return convertView;
    }

    public void removeEnabled()
    {
        checkBox.setVisibility(View.VISIBLE);
    }

    public void removeDisabled()
    {
        checkBox.setVisibility(View.GONE);
    }
}
