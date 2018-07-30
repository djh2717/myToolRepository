package my.utils;

import android.util.TypedValue;

/**
 * An px util, use to unit conversion.
 *
 * @author 15445
 */
public class PxUtil {
    public static float mmToPx(int mm) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm, MyApplication.getContext().getResources().getDisplayMetrics());
    }

    public static float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getContext().getResources().getDisplayMetrics());
    }

    public static float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, MyApplication.getContext().getResources().getDisplayMetrics());
    }
}
