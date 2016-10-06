package fragment;

import android.content.ContentResolver;

import asyncTask.ContactAst;

/**
 * Created by lequan on 4/28/2016.
 */
public class ContactFragment extends DestinationFragment
{

    public ContactFragment()
    {
    }

    @Override
    void initAsyncTask()
    {
        asyncTask = new ContactAst(getActivity().getContentResolver());
    }

    @Override
    void onSelected(int position)
    {
        listener.onSelected(list.get(position).getAddress());
    }
}
