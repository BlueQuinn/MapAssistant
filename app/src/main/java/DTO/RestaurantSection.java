package DTO;

/**
 * Created by lequan on 5/8/2016.
 */
public class RestaurantSection
{
    String name;
    String url;
    public String getName()
    {
        return name;
    }

    public int getImage()
    {
        return image;
    }

    public String getUrl()
    {
        return url;
    }

    public RestaurantSection(String name, String url, int image)
    {

        this.name = name;
        this.url = url;
        this.image = image;
    }

    int image;
}
