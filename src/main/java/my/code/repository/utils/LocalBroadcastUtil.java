package my.code.repository.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * A local broadcast util, local broadcast can not use static register.
 * notice: After android.O, cancel almost custom static register broadcast receiver.
 * notice: If you also want register static receiver, you should do this "intent.setComponent(param1,param2)"
 * param1: Receiver location package.
 * param2: Receiver full class name.
 *
 * @author Djh on 2018/7/27 11:05
 * E-Mail ：1544579459@qq.com
 */
public class LocalBroadcastUtil {

    @SuppressLint("StaticFieldLeak")
    private static final Context CONTEXT = MyApplication.getContext();

    private static LocalBroadcastManager sLocalBroadcastManager =
            LocalBroadcastManager.getInstance(CONTEXT);


    /**
     * Send a local broadcast.
     */
    public static void sendLocalBroadcast(String action) {
        sLocalBroadcastManager.sendBroadcast(new Intent(action));
    }

    /**
     * Overload method.
     */
    public static void sendLocalBroadcast(Intent intent) {
        sLocalBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Dynamic register a local broadcast receiver.
     */
    public static void registerReceiver(String action, BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter(action);
        sLocalBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    /**
     * Unregister receiver.
     */
    public static void unRegisterReceiver(BroadcastReceiver receiver) {
        sLocalBroadcastManager.unregisterReceiver(receiver);
    }
}
