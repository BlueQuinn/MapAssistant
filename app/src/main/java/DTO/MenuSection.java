package DTO;

import java.util.ArrayList;

/**
 * Created by Quan-DevTeam on 11/9/15.
 */
public class MenuSection
{
    String title;
    ArrayList<Menu> listSubtitle;

    public MenuSection(String title, ArrayList<Menu> listSubtitle) {
        this.title = title;
        this.listSubtitle = listSubtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Menu> getListSubtitle() {
        return listSubtitle;
    }

}
