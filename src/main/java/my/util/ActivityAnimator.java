package my.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Window;

/**
 * Alter activity default transition animation.
 *
 * @author djh
 */
public class ActivityAnimator {

    public static void startActivity(Intent intent, Activity activity) {
        activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
    }

    public static void startActivityForResult(Intent intent, int requestCode, Activity activity) {
        activity.startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
    }

    public static void requestAndSetExplode(Activity activity) {
        activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        activity.getWindow().setEnterTransition(new Explode());
        activity.getWindow().setExitTransition(new Explode());
    }

    public static void requestAndSetSlide(Activity activity) {
        activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        activity.getWindow().setEnterTransition(new Slide());
        activity.getWindow().setExitTransition(new Slide());
    }
}
