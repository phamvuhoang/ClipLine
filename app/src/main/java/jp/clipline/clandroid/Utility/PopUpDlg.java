/**
 * PopUpDlg
 * Common class use to show Popup dialog with variant ways
 *
 * @author Briswell - Do Anh Tuan
 * @version 1.0 2016-08-18.
 */


package jp.clipline.clandroid.Utility;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class PopUpDlg {

    private AlertDialog mDlg;
    private Context mContext;
    private AlertDialog alertDialog;


    /**
     * Constructor
     *
     * @param context
     * @param modal   if modal is true then user must close popup by interacting with controls
     *                inside popup. Touching out side popup to canceled is not permit
     */
    public PopUpDlg(Context context, boolean modal) {
        if (context != null) {
            mDlg = new AlertDialog.Builder(context).create();
            mDlg.setCanceledOnTouchOutside(!modal);
            mContext = context;
            //Here's the magic..
            //Set the dialog to not focusable (makes navigation ignore us adding the window)
//            mDlg.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        } else {
            throw new RuntimeException("Context is null. Need a context to create Popup!");
        }
    }

    /**
     * Show common popup with message and return response for caller by callback function
     * that mean caller must implement callback function to get response from user and do
     * business logic
     *
     * @param title
     * @param msg
     * @param positiveBnt positive button title
     * @param negativeBnt negative button title
     * @param onOK
     * @param onCancel
     */
    public void show(String title, String msg, String positiveBnt, String negativeBnt,
                     final DialogInterface.OnClickListener onOK,
                     final DialogInterface.OnClickListener onCancel) {

        if (mDlg != null) {
            mDlg.setTitle(title);
            mDlg.setMessage(msg);
            if (!TextUtils.isEmpty(positiveBnt)) {
                mDlg.setButton(DialogInterface.BUTTON_POSITIVE, positiveBnt, onOK);
            }
            if (!TextUtils.isEmpty(negativeBnt)) {
                mDlg.setButton(DialogInterface.BUTTON_NEGATIVE, negativeBnt, onCancel);
            }
            mDlg.show();
        }
    }

}
