package DTO;

/**
 * Created by lequan on 5/14/2016.
 */
public class Jam
{
    String time;
    int vote;

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public int getVote()
    {
        return vote;
    }

    public void setVote(int vote)
    {
        this.vote = vote;
    }

    public Jam(String time, int vote)
    {

        this.time = time;
        this.vote = vote;
    }
}
