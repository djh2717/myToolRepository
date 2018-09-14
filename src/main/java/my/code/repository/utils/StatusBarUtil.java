package my.code.repository.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

/**
 * A status bar util,use to setStatusBar status bar and get status bar height, also can hide
 * bottom navigation.
 *
 * @author Djh on 2018/7/23 14:48
 * E-Mail ：1544579459@qq.com
 */
public class StatusBarUtil {

    public static int getStatusBarHeight() {
        int height = 0;
        //获取资源ID
        int resourceId = MyApplication.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = MyApplication.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static void setTransparent(Activity activity) {
        setStatusBar(activity, false);
    }

    public static void hideNavigation(Activity activity) {
        setStatusBar(activity, true);
    }

    // ------------------ Internal API ------------------

    private static void setStatusBar(Activity activity, boolean hide) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View view = activity.getWindow().getDecorView();
            if (hide) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            } else {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
