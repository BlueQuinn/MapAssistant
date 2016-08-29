package DTO;

/**
 * Created by lequan on 5/8/2016.
 */
public class Food
{
    String food;
    String price;

    public String getFood()
    {
        return food;
    }

    public String getPrice()
    {
        return price;
    }

    public String getImage()
    {
        return image;
    }

    public Food(String food, String price, String image)
    {

        this.food = food;
        this.price = price;
        this.image = image;
    }

    String image;

}
