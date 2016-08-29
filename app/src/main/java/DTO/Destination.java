package DTO;

import java.io.Serializable;

/**
 * Created by lequan on 4/23/2016.
 */
public class Destination implements Serializable
{
    String name;

    public Destination(String name, String address)
    {
        this.name = name;
        this.address = address;
    }

    String address;

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

}
