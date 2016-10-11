package model;

/**
 * Created by lequan on 10/10/2016.
 */
public class Petrol
{
    String name, price;
    int icon;

    public String getName()
    {
        return name;
    }

    public String getPrice()
    {
        return price + " đồng/lít";
    }

    public int getIcon()
    {
        return icon;
    }

    public Petrol(String name, String price)
    {

        this.name = name;
        this.price = price;
    }
}
