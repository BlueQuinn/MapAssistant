package DTO;

/**
 * Created by Quan-DevTeam on 11/9/15.
 */
public class Menu
{


    public String getTitle() {
        return title;
    }
    String title;
    int icon;

    public int getIcon() {
        return icon;
    }

    public Menu(String title, int icon)
    {
        this.title = title;
        this.icon = icon;
    }
}

