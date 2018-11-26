package my.code.repository.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;

/**
 * A net work util, use to judging network status.
 *
 * @author djh on  2018/8/1 11:40
 * @E-Mail 1544579459@qq.com
 */
public class NetWorkUtil {

    /**
     * If net work is connect, return true.
     */
    public static boolean isNetWorkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // notice: the isAvailable and isConnected is difference!
        // 1. isConnected represent can data transmission.
        // 2. isAvailable represent have Internet signal, but does not mean that
        // data transmission is possible.
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}
