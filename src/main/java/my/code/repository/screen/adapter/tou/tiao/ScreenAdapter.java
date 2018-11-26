package my.code.repository.screen.adapter.tou.tiao;

import android.app.Activity;
import android.app.Application;
import android.util.DisplayMetrics;

/**
 * Use the 360dp, because the 1080px, and 480dpi is the main stream.
 *
 * @author djh on  2018/10/14 17:09
 * @E-Mail 1544579459@qq.com
 */
public class ScreenAdapter {
    public static void customDensity(Activity activity, Application application) {
        // Application.
        DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();

        int targetDensity = appDisplayMetrics.widthPixels / 360;
        int targetScaleDensity = (int) (targetDensity * (appDisplayMetrics.scaledDensity / appDisplayMetrics.density));
        int targetDensityDpi = targetDensity * 160;

        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaleDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        // Activity.
        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();

        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaleDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }
}
