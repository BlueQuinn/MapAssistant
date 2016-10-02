package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import model.Restaurant;

/**
 * Created by lequan on 2/1/2016.
 */
public class RestaurantAdt extends ArrayAdapter<model.Restaurant>
{

    Context context;
    int resource;
    List<Restaurant> list;

    public RestaurantAdt(Context context, int resource, List<Restaurant> list)
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

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
        ImageView image = (ImageView) convertView.findViewById(R.id.imvThumbnail);

        tvTitle.setText(list.get(position).getTitle());
        tvAddress.setText(list.get(position).getAddress());

        UrlImageViewHelper.setUrlDrawable(image, list.get(position).getImg());

        return convertView;
    }

}

