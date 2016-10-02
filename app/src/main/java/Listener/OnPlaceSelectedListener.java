package listener;

/**
 * Created by lequan on 4/27/2016.
 */
public interface OnPlaceSelectedListener
{
    void onSelected(String fragment, String name, String address);
    void onSelected(String address);
}
