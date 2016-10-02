package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import model.MenuSection;
import model.Menu;
import com.bluebirdaward.mapassistant.gmmap.R;


/**
 * Created by Quan-DevTeam on 11/9/15.
 */
public class MenuAdt extends BaseExpandableListAdapter
{
    Context context;
    int resourceRow;
    int resourceSection;
    ArrayList<MenuSection> listSection;

    public MenuAdt(Context context, int resourceRow, int resourceSection, ArrayList<MenuSection> listMenuIem)
    {
        this.context = context;
        this.resourceRow = resourceRow;
        this.resourceSection = resourceSection;
        this.listSection = listMenuIem;
    }

    @Override
    public int getGroupCount()
    {
        return listSection.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return listSection.get(groupPosition).getListSubtitle().size();
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        convertView = LayoutInflater.from(context).inflate(resourceSection, parent, false);

        MenuSection item = listSection.get(groupPosition);
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(item.getTitle());

        ExpandableListView mExpandableListView = (ExpandableListView) parent;
        mExpandableListView.expandGroup(groupPosition);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(resourceRow, parent, false);
        }

        //lay item thu position va set data
        MenuSection item = listSection.get(groupPosition);
        ArrayList<Menu> listMenu = item.getListSubtitle();

        Menu subitem = listMenu.get(childPosition);
        ((TextView) convertView.findViewById(R.id.tvSubTitle)).setText(subitem.getTitle());
        ((ImageView) convertView.findViewById(R.id.imgIcon)).setImageResource(subitem.getIcon());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return 0;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }
}

