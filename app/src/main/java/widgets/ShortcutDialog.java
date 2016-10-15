package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.MainActivity;
import com.bluebirdaward.mapassistant.gmmap.R;


import listener.OnLoadListener;
import model.Shortcut;
import utils.RouteUtils;

/**
 * Created by lequan on 10/12/2016.
 */
public class ShortcutDialog extends Dialog implements View.OnClickListener
{
    int distance, duration, rating, time, ID;
    String jamType;
    boolean like;
    TextView txtRating;

    // LinearLayout btnDislike, btnLike,btnAdd;
    ImageButton btnDislike, btnLike, btnAdd;

    OnLoadListener<Integer> listener;

    public ShortcutDialog(Context context, Shortcut shortcut, int time, String jamType, int ID)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_shorcut);

        this.time = time;
        this.jamType = jamType;
        this.ID = ID;
        distance = shortcut.getDistance();
        duration = shortcut.getDuration();
        rating = shortcut.getRating();

        TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
        TextView txtDuration = (TextView) findViewById(R.id.txtDuration);
        txtRating = (TextView) findViewById(R.id.txtRating);

        txtDistance.setText("Chiều dài: " + RouteUtils.getDistance(distance));
        txtDuration.setText("Thời gian đi: " + RouteUtils.getDuration(duration));

        if (like)
        {
            like();
        }
        else
        {
            dislike();
        }

      /*  btnDislike = (LinearLayout) findViewById(R.id.btnDislike);
        btnLike = (LinearLayout) findViewById(R.id.btnLike);
        btnAdd = (LinearLayout) findViewById(R.id.btnAdd);*/
        btnDislike = (ImageButton) findViewById(R.id.btnDislike);
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
                dislike();
                MainActivity.sqlite.dislike(time, jamType, ID, rating);

                btnDislike.setVisibility(View.GONE);
                btnLike.setVisibility(View.VISIBLE);
                break;

            case R.id.btnLike:
                rating++;
                listener.onFinish(rating);
               like();
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

    void like()
    {
        if (rating == 1)
        {
            txtRating.setText("Bạn đã thích đường đi này");
        }
        else
        {
            txtRating.setText("Bạn và " + Integer.toString(rating) + " người khác đã thích đường đi này");
        }
    }

    void dislike()
    {
        if (rating == 0)
        {
            txtRating.setText("Hãy là người đầu tiên thích đường đi này");
        }
        else
        {
            txtRating.setText(Integer.toString(rating) + " người đã thích đường đi này");
        }
    }

}
