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

import model.Food;
import model.Petrol;

/**
 * Created by lequan on 2/1/2016.
 */
public class PetrolAdt extends ArrayAdapter<Petrol>
{

    Context context;
    int resource;
    List<Petrol> list;

    public PetrolAdt(Context context, int resource, List<Petrol> list)
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

        TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        TextView txtPrice = (TextView) convertView.findViewById(R.id.txtPrice);
        ImageView image = (ImageView) convertView.findViewById(R.id.imgIcon);

        txtName.setText(list.get(position).getName());
        txtPrice.setText(list.get(position).getPrice());
        image.setImageResource(list.get(position).getIcon());
/*

        if (list.get(position).getImage().length() > 0)
        {
            UrlImageViewHelper.setUrlDrawable(image, list.get(position).getImage());
        }
*/

        return convertView;
    }

}

