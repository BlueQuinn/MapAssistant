package DTO;

import java.io.Serializable;

/**
 * Created by mac on 10/13/15.
 */
public class Restaurant implements Serializable
{
    String title;
    String img;
    String address;
    String url;

    public String getUrl()
    {
        return url;
    }

    public Restaurant(String title, String img, String address, String url)
    {
        this.title = title;
        this.img = img;
        this.address = address;
        this.url = url;
    }

    public String getAddress()
    {
        return address;
    }

    public String getTitle()
    {
        return title;
    }

    public String getImg()
    {
        return img;
    }

}
