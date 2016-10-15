package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.MainActivity;
import com.bluebirdaward.mapassistant.gmmap.R;


import listener.OnLoadListener;
import model.Shortcut;

/**
 * Created by lequan on 10/12/2016.
 */
public class ShortcutDialog extends Dialog implements View.OnClickListener
{
    int distance, duration, rating, time, ID;
    String jamType;
    boolean like;
    TextView txtRating;
    ImageButton btnDislike, btnLike,btnAdd;

    OnLoadListener<Integer> listener;

    public ShortcutDialog(Context context, Shortcut shortcut, int time, String jamType, int ID)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);

        this.time = time;
        this.jamType = jamType;
        this.ID = ID;
        distance = shortcut.getDistance();
        duration = shortcut.getDuration();
        rating = shortcut.getRating();

        TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
        TextView txtDuration = (TextView) findViewById(R.id.txtDuration);
        txtRating = (TextView) findViewById(R.id.txtRating);

        txtDistance.setText(Integer.toString(distance));
        txtDuration.setText(Integer.toString(duration));

        if (like)
        {
            txtRating.setText("Bạn và " + Integer.toString(rating) + " người khác đã thích đường đi này");
        }
        else
        {
            txtRating.setText(Integer.toString(rating) + " người đã thích đường đi này");
        }

        btnDislike = (ImageButton) findViewById(R.id.btnDirection);
        btnLike = (ImageButton) findViewById(R.id.btnLike);
        btnAdd = (ImageButton) findViewById(R.id.btnAdd);

        btnDislike.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        if (MainActivity.sqlite.checkLiked(time, jamType, ID))
        {
           btnDislike.setVisibility(View.VISIBLE);
            btnLike.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnDislike:
                rating--;
                listener.onFinish(rating);
                txtRating.setText(Integer.toString(rating) + " người đã thích đường đi này");
                MainActivity.sqlite.dislike(time, jamType, ID, rating);

                btnDislike.setVisibility(View.GONE);
                btnLike.setVisibility(View.VISIBLE);
                break;

            case R.id.btnLike:
                rating++;
                listener.onFinish(rating);
                txtRating.setText("Bạn và " + Integer.toString(rating) + " người khác đã thích đường đi này");
                MainActivity.sqlite.like(time, jamType, ID, rating);

                btnDislike.setVisibility(View.VISIBLE);
                btnLike.setVisibility(View.GONE);
                break;

            case R.id.btnAdd:
                listener.onFinish(-1);
                dismiss();
                break;
        }
    }

    public void setListener(OnLoadListener<Integer> listener)
    {
        this.listener = listener;
    }
}
