package my.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Window;

/**
 * An activity util, use to start activity or start activityForResult and custom
 * the activity transition animator. This is only adapter no intent extra data.
 *
 * @author djh
 */
public class ActivityUtil {

    /**
     * Use the custom transition animator of activity.
     */
    public static void startActivityCustomAnimator(Activity fromActivity, Class toActivity) {
        //Custom animator direct start activity no need result.
        startDirect(fromActivity, toActivity, false, true, -1);
    }

    /**
     * Overload method -2.
     */
    public static void startActivityCustomAnimator(Activity fromActivity, Class toActivity, int requestCode) {
        //Custom animator direct start activity need result.
        startDirect(fromActivity, toActivity, true, true, requestCode);
    }

    /**
     * Overload method -3.
     */
    public static void startActivityCustomAnimator(Activity fromActivity, String intentAction) {
        //Custom animator hermit start activity no need result.
        startHermit(fromActivity, intentAction, false, true, -1);
    }

    /**
     * Overload method -4.
     */
    public static void startActivityCustomAnimator(Activity fromActivity, String intentAction, int requestCode) {
        //Custom animator hermit start activity need result.
        startHermit(fromActivity, intentAction, true, true, requestCode);
    }


    /**
     * Start direct activity.
     */
    public static void startActivity(Activity fromActivity, Class toActivity) {
        //Direct start activity no need result.
        startDirect(fromActivity, toActivity, false, false, -1);
    }

    /**
     * Overload method.
     */
    public static void startActivity(Activity fromActivity, Class toActivity, int requestCode) {
        //Direct start activity need result.
        startDirect(fromActivity, toActivity, true, false, requestCode);
    }


    /**
     * Used to start hermit activity.
     */
    public static void startHermitActivity(Activity fromActivity, String intentAction) {
        //Hermit start activity no need result.
        startHermit(fromActivity, intentAction, false, false, -1);
    }

    /**
     * Overload method.
     */
    public static void startHermitActivity(Activity fromActivity, String intentAction, int requestCode) {
        //Hermit start activity need result.
        startHermit(fromActivity, intentAction, true, false, requestCode);
    }


    private static void startDirect(Activity fromActivity, Class toActivity, boolean needResult, boolean customAnimator, int requestCode) {
        Intent intent = new Intent(fromActivity, toActivity);
        if (needResult) {
            if (customAnimator) {
                //Custom activity start animator.
                fromActivity.startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(fromActivity).toBundle());
            } else {
                fromActivity.startActivityForResult(intent, requestCode);
            }
        } else {
            if (customAnimator) {
                fromActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(fromActivity).toBundle());
            } else {
                fromActivity.startActivity(intent);
            }
        }
    }

    private static void startHermit(Activity fromActivity, String intentAction, boolean needResult, boolean customAnimator, int requestCode) {
        Intent intent = new Intent(intentAction);
        PackageManager packageManager = fromActivity.getPackageManager();
        ComponentName componentName = intent.resolveActivity(packageManager);

        //Judge the intent can deal with some activity, if can't then open google store.
        if (componentName != null) {
            if (needResult) {
                if (customAnimator) {
                    //Custom activity start animator.
                    fromActivity.startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(fromActivity).toBundle());
                } else {
                    fromActivity.startActivityForResult(intent, requestCode);
                }
            } else {
                if (customAnimator) {
                    fromActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(fromActivity).toBundle());
                } else {
                    fromActivity.startActivity(intent);
                }
            }
        } else {
            Uri marketUri = Uri.parse("market://search?q=pname:com.myapp.packagename");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(marketUri);
            // If the Google Play Store is available, use it to download an application
            if (marketIntent.resolveActivity(packageManager) != null) {
                fromActivity.startActivity(marketIntent);
            } else {
                ToastUtil.showToast("应用市场不可用,启动失败");
            }
        }
    }

    /**
     * Set the toActivity transition animator.
     * Use this before toActivity setContentView().
     */
    public static void setExplodeAnimator(Activity activity) {
        activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        activity.getWindow().setEnterTransition(new Explode());
        activity.getWindow().setExitTransition(new Explode());
    }

    /**
     * Set the toActivity transition animator.
     * Use this before toActivity setContentView().
     */
    public static void setSlideAnimator(Activity activity) {
        activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        activity.getWindow().setEnterTransition(new Slide());
        activity.getWindow().setExitTransition(new Slide());
    }
}
