package Fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;

import Adapter.DestinationAdt;
import AsyncTask.DestinationAst;
import DTO.Destination;
import Listener.OnLoadListener;
import Listener.OnPlaceSelectedListener;
import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 4/27/2016.
 */
public class DestinationFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
    ListView lvDestination;
    ArrayList<Destination> list;
    DestinationAdt adapter;
    OnPlaceSelectedListener listener;
    ProgressBar prbLoading;
    boolean loaded;
    DestinationAst asyncTask;

    public DestinationFragment()
    {
        loaded = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        init();

        list = new ArrayList<>();
        loadDestination();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View convertView = inflater.inflate(R.layout.fragment_destination, container, false);

        prbLoading = (ProgressBar) convertView.findViewById(R.id.prbLoading);

        adapter = new DestinationAdt(getActivity().getApplicationContext(), R.layout.row_destination, list);
        lvDestination = (ListView) convertView.findViewById(R.id.lvDestination);
        lvDestination.setAdapter(adapter);
        lvDestination.setOnItemClickListener(this);

        return convertView;
    }

    void init()     // do not delete this
    {

    }

    void initAsyncTask()    // do not delete this
    {

    }

    void loadDestination()
    {
        initAsyncTask();
        asyncTask.setOnLoadListener(new OnLoadListener<ArrayList<Destination>>()
        {
            @Override
            public void onFinish(ArrayList<Destination> listDestination)
            {
                prbLoading.setVisibility(View.GONE);
                //for (int i = 0; i < listDestination.size(); ++i)
                for (int i = listDestination.size() - 1; i > -1; --i)
                {
                    list.add(listDestination.get(i));
                }
                adapter.notifyDataSetChanged();
                lvDestination.setVisibility(View.VISIBLE);
            }
        });

        asyncTask.execute();
    }

    public void setOnPlaceSelectedListener(OnPlaceSelectedListener listener)
    {
        this.listener = listener;
    }

    void onSelected(int position)
    {
        listener.onSelected(list.get(position).getAddress());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        onSelected(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        for (int i=0;i<adapter.getCount();++i)
        {
adapter.removeEnabled();
        }
        return false;
    }

    public void remove()
    {
        for(Iterator<Destination> iterator = list.iterator(); iterator.hasNext(); ) {
            if(iterator.next().isCheck())
                iterator.remove();
        }
        adapter.notifyDataSetChanged();
    }
}
