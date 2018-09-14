package my.code.repository.utils;

import android.view.KeyEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A key down util, use to process the back key is click by user, we show a toast let user
 * click the key twice then we use the super method to finish the activity.
 *
 * @author djh on  2018/9/10 20:33
 * @E-Mail 1544579459@qq.com
 */
public class KeyDownUtil {

    /**
     * Use to mark whether should finish the activity when the use click back key.
     */
    private static boolean sFinish = false;

    /**
     * Use to timing reset the sFinish mark.
     */
    private static ExecutorService sThreadPool;

    /**
     * When the code is back code, and user within three seconds click twice the back,
     * we will return true mean the we should call super method to finish the activity,
     * otherwise we will return false.
     * <li></li>If the key code is not back code, we will also return true mean use super method
     * to process the keyEvent.
     * <li></li><p><b>Use this at activity onKeyDown like the following:</b></p>
     * <pre>{@code
     * public boolean onKeyDown(int keyCode, KeyEvent event) {
     *       return !KeyDownUtil.backKeyFinish(keyCode) ||
     *           super.onKeyDown(keyCode, event);
     * }
     * }</pre>
     */
    public static boolean backKeyFinish(int keyCode) {

        // If user within three seconds click the back twice, we should return true.
        if (sFinish && keyCode == KeyEvent.KEYCODE_BACK) {

            // If finish, we should shut down the threadPool.
            sThreadPool.shutdownNow();
            sThreadPool = null;

            // Then we return true.
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_BACK) {

            // Init the thread pool first.
            if (sThreadPool == null) {
                sThreadPool = Executors.newSingleThreadExecutor();
            }

            // Otherwise we should show a click again toast.
            ToastUtil.showToast("再按一次退出");

            // Then we timing 3 seconds, if user click again we set the mark as true.
            sThreadPool.execute(() -> {
                sFinish = true;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sFinish = false;
            });

            return false;
        }

        // If the code not is back code, we do not process.
        return true;
    }
}
