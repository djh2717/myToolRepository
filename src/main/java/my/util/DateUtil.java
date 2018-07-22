package my.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Use to get now time or date.
 * Get the format of the date "yyyy-MM-dd HH:mm:ss"
 *
 * @author 15445
 */
public class DateUtil {

    public static final int TIME_ALL = 0;
    public static final int TIME_HOUR = 1;
    public static final int TIME_MINUTE = 2;
    public static final int TIME_SECOND = 3;

    public static final int DATE_ALL = 0;
    public static final int DATE_YEAR = 1;
    public static final int DATE_MONTH = 2;
    public static final int DATE_DAY = 3;

    public static String getNowTime(int type) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String nowTime = simpleDateFormat.format(date);
        String[] strings = nowTime.split(":");
        switch (type) {
            case TIME_ALL:
                return nowTime;
            case TIME_HOUR:
                return strings[0];
            case TIME_MINUTE:
                return strings[1];
            case TIME_SECOND:
                return strings[2];
            default:
        }
        return null;
    }

    public static String getNowDate(int type) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String nowDate = simpleDateFormat.format(date);
        String[] strings = nowDate.split("-");
        switch (type) {
            case DATE_ALL:
                return nowDate;
            case DATE_YEAR:
                return strings[0];
            case DATE_MONTH:
                return strings[1];
            case DATE_DAY:
                return strings[2];
            default:
        }
        return null;
    }

}
