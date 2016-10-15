package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 10/7/2016.
 */
public class MessageDialog extends Dialog
{
    LinearLayout contentLayout;
    RelativeLayout loadingLayout;

    public static void showMessage(Context context, int color, int icon, String title, String info)
    {
        MessageDialog dialog = new MessageDialog(context, color, icon, title, info);
        dialog.show();
    }

    public MessageDialog(Context context, int color, int icon, String title, String info)
    {
        super(context);
        initLayout();
        show(  color,  icon,  title,  info);
    }

    public MessageDialog(Context context)
    {
        super(context);
        initLayout();

        //contentLayout.setVisibility(View.VISIBLE);
        //setCanceledOnTouchOutside(false);
        loading();
    }

    void initLayout()
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_message);

        contentLayout = (LinearLayout) findViewById(R.id.contentLayout);
        loadingLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
    }

    public void loading()
    {
        contentLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    public void show( int color, int icon, String title, String info)
    {
        loadingLayout.setVisibility(View.GONE);
        LinearLayout layoutTitle = (LinearLayout) findViewById(R.id.layoutTitle);
        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        TextView txtInfo = (TextView) findViewById(R.id.txtInfo);
        ImageView imvIcon = (ImageView) findViewById(R.id.imvIcon);

        layoutTitle.setBackgroundColor(color);
        txtTitle.setText(title);
        txtInfo.setText(info);
        imvIcon.setImageResource(icon);
        contentLayout.setVisibility(View.VISIBLE);
    }
}
