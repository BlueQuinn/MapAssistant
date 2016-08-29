package Fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import AsyncTask.ContactAst;
import DTO.Destination;

/**
 * Created by lequan on 4/28/2016.
 */
public class ContactFragment extends DestinationFragment
{
    ContentResolver contentResolver;

    public ContactFragment()
    {

    }

    @Override
    void initAsyncTask()
    {
        asyncTask = new ContactAst(contentResolver);
    }

    @Override
    void init()
    {
        contentResolver = getActivity().getContentResolver();
    }

}
