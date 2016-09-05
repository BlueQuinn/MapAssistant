package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import model.RestaurantSection;
import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 2/1/2016.
 */
public class RestaurantSectionAdt extends ArrayAdapter<RestaurantSection>
{

    Context context;
    int resource;
    List<RestaurantSection> list;

    public RestaurantSectionAdt(Context context, int resource, List<RestaurantSection> list)
    {
        super(context, resource, list);
        this.context = context;
        this.resource = resource;
        this.list = list;
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
        {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.tvPlace);
        ImageView image = (ImageView) convertView.findViewById(R.id.imvPlace);

        title.setText(list.get(position).getName());
        Picasso.with(context).load(list.get(position).getImage()).into(image);
        //image.setImageResource(list.get(position).getImage());
        return convertView;
    }

}

