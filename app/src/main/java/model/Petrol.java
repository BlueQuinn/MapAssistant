package model;

/**
 * Created by SonPham on 10/14/2016.
 */
public class Petrol {
    String name, price;
int icon;

    public void setIcon(int icon)
    {
        this.icon = icon;
    }

    public Petrol(String name, String price) {
        this.name = name;
        this.price = price;

    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price + " đồng/lít";
    }

    public int getIcon() {
        return icon;
    }
}
