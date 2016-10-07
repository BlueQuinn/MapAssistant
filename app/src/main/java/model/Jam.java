package model;

/**
 * Created by lequan on 5/14/2016.
 */
public class Jam
{
    String start, end;
    int vote;

    public void setStart(String start)
    {
        this.start = start;
    }

    public void setEnd(String end)
    {
        this.end = end;
    }

    public void setVote(int vote)
    {
        this.vote = vote;
    }

    public String getStart()
    {

        return start;
    }

    public String getEnd()
    {
        return end;
    }

    public int getVote()
    {
        return vote;
    }

    public Jam(String start, String end, int vote)
    {

        this.start = start;
        this.end = end;
        this.vote = vote;
    }
}
