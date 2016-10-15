package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.List;

import model.WeatherTime;
import model.WeatherWeek;

/**
 * Created by lequan on 2/1/2016.
 */
public class WeatherTimeAdt extends ArrayAdapter<WeatherTime>
{

    Context context;
    int resource;
    List<WeatherTime> list;

    public WeatherTimeAdt(Context context, int resource, List<WeatherTime> list)
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

        TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
        TextView txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
        TextView txtTemperature = (TextView) convertView.findViewById(R.id.txtTemperature);
        TextView txtHumidity = (TextView) convertView.findViewById(R.id.txtHumidity);
        TextView txtRain = (TextView) convertView.findViewById(R.id.txtRain);
        ImageView image = (ImageView) convertView.findViewById(R.id.imgIcon);

        txtDate.setText(list.get(position).getTime());
        txtInfo.setText(list.get(position).getInfo());
        txtTemperature.setText(list.get(position).getTemperature());
        txtHumidity.setText(list.get(position).getHumidity());
        txtRain.setText(list.get(position).getRain());
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
