package widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.List;

/**
 * Created by lequan on 9/5/2016.
 */
public class RowDestination extends LinearLayout implements Checkable
{
    //public static final int[] CHECKED_STATE = {R.attr.state_checked};
    boolean isChecked;
    List<Checkable> checkableViews;

    public RowDestination(Context context)
    {
        super(context);
    }

    public RowDestination(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RowDestination(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setChecked(boolean checked)
    {
        isChecked = checked;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked()
    {
        return isChecked;
    }

    @Override
    public void toggle()
    {
        isChecked = !isChecked;
        refreshDrawableState();
    }

    /*@Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        int[] states =  super.onCreateDrawableState(extraSpace + 1);
        if (isChecked){
            mergeDrawableStates(states, CHECKED_STATE);
        }
        return states;
    }*/
}
