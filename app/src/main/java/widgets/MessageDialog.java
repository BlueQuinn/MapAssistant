package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 10/7/2016.
 */
public class MessageDialog extends Dialog
{
    public static void showError(Context context, String title, String info)
    {
        //MessageDialog dialog = new MessageDialog(context, context.getResources().getColor(R.color.colorPrimaryDark), context.getResources().getDrawable(R.drawable.error), title, info);
        //dialog.show();
    }

    public static void showMessage(Context context, int color, int icon, String title, String info)
    {
        MessageDialog dialog = new MessageDialog(context, color, icon, title, info);
        dialog.show();
    }

    public MessageDialog(Context context, int color, int icon, String title, String info)
    {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_message);

        LinearLayout layoutTitle = (LinearLayout) findViewById(R.id.layoutTitle);
        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        TextView txtInfo = (TextView) findViewById(R.id.txtInfo);
        ImageView imvIcon = (ImageView) findViewById(R.id.imvIcon);

        layoutTitle.setBackgroundColor(color);
        txtTitle.setText(title);
        txtInfo.setText(info);
        imvIcon.setImageResource(icon);

        //setCanceledOnTouchOutside(false);
    }
}
