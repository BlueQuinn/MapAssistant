package Fragment;

import AsyncTask.DestinationAst;

/**
 * Created by lequan on 4/28/2016.
 */
public class FavouriteFragment extends DestinationFragment
{
    public FavouriteFragment()
    {

    }

    @Override
    void initAsyncTask()
    {
        asyncTask = new DestinationAst("Favourite");
    }

    @Override
    void onSelected(int position)
    {
        listener.onSelected(list.get(position).getName());
    }
}
