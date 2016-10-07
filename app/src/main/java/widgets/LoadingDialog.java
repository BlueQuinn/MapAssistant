package widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.bluebirdaward.mapassistant.gmmap.R;

/**
 * Created by lequan on 10/7/2016.
 */
public class LoadingDialog extends Dialog
{
    public LoadingDialog(Context context)
    {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);

        setCanceledOnTouchOutside(false);
    }
}
