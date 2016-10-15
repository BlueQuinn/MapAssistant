package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 10/7/2016.
 */
public class LoadingDialog extends Dialog
{
    public static LoadingDialog show(Context context, String message)
    {
        return new LoadingDialog(context, message);
    }

    ProgressBar prbLoading;
    TextView txtMessage;
    public LoadingDialog(Context context, String message)
    {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);

        setCanceledOnTouchOutside(false);

        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        txtMessage  = (TextView) findViewById(R.id.txtMessage);
        txtMessage.setText(message);
    }

    public void dismiss(String message)
    {
        prbLoading.setVisibility(View.GONE);
        txtMessage.setText(message);
    }
}
