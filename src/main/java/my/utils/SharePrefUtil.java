package my.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Djh on 2018/7/23 23:47
 * E-Mail ：1544579459@qq.com
 */
public class SharePrefUtil {

    public static SharedPreferences get() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
    }

    public static SharedPreferences.Editor put() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
    }
}
