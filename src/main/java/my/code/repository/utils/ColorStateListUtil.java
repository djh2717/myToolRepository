package my.code.repository.utils;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorRes;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A util use to get color state list from xml.
 *
 * @author djh on  2018/9/11 14:01
 * @E-Mail 1544579459@qq.com
 */
public class ColorStateListUtil {

    /**
     * Use to get the color state list from xml, if an exception is occur will return null.
     */
    @SuppressLint("ResourceType")
    public static ColorStateList get(@ColorRes int resourceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return MyApplication.getContext().getColorStateList(resourceId);
        } else {
            Resources resources = MyApplication.getContext().getResources();
            try {
                return ColorStateList.createFromXml(resources, resources.getXml(resourceId));
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
