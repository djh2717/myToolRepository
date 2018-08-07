package my.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Toast skill, avoid multiple triggers lead to long time display toast.
 *
 * @author Djh on 2018/7/22 16:19
 * E-Mail ：1544579459@qq.com
 */
public class ToastUtil {
    private static Toast toast;
    private static Snackbar sSnackbar;

    @SuppressLint("ShowToast")
    public static void showToast(String content) {
        if (toast == null) {
            toast = Toast.makeText(MyApplication.getContext(), content, Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    public static void showSnackbar(String content, ViewGroup viewGroup, @Nullable String actionContent, @Nullable View.OnClickListener onClickListener) {
        if (sSnackbar == null) {
            sSnackbar = Snackbar.make(viewGroup, content, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.WHITE);
        } else {
            sSnackbar.setActionTextColor(Color.WHITE);
            sSnackbar.setText(content);
        }
        if (onClickListener != null) {
            sSnackbar.setAction(actionContent, onClickListener);
        }
        sSnackbar.show();
    }

}
