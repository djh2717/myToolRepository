package my.util;

import android.annotation.SuppressLint;
import android.widget.Toast;

/**
 * Toast skill, avoid multiple triggers lead to long time display toast.
 *
 * @author Djh on 2018/7/22 16:19
 * E-Mail ：1544579459@qq.com
 */
public class ToastUtil {
    private static Toast toast;

    @SuppressLint("ShowToast")
    public static void showToast(String content) {
        if (toast == null) {
            toast = Toast.makeText(MyApplication.getContext(), content, Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
