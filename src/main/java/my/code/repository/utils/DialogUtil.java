package my.code.repository.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A dialogUtil, use to show alertDialog and progressDialog.
 *
 * @author Djh on 2018/7/24 16:55
 * E-Mail ：1544579459@qq.com
 */
public class DialogUtil {

    private static ProgressDialog progressDialog;
    private static ScheduledExecutorService executorService;

    public static AlertDialog.Builder showAlertDialog(Context context, CharSequence content, CharSequence title, int iconId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(iconId);
        builder.setTitle(title);
        builder.setMessage(content);
        return builder;
    }

    /**
     * Show a progressDialog, use the ScheduledThreadPool to dynamic modify the message
     * of progressDialog.
     */
    public static void showProgressDialog(Context context, int iconId, boolean cancelable) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIcon(iconId);
        progressDialog.setTitle("加载");
        progressDialog.setCancelable(cancelable);
        progressDialog.setMessage("正在加载,请稍后...");
        progressDialog.setCanceledOnTouchOutside(false);
        //If djh cancel the progressDialog, should stop the scheduled thread.
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                executorService.shutdownNow();
            }
        });

        //Use ScheduledThreadPool to modify the message of progressDialog.
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                i++;
                switch (i) {
                    case 1:
                        progressDialog.setMessage("正在加载,请稍后.");
                        break;
                    case 2:
                        progressDialog.setMessage("正在加载,请稍后..");
                        break;
                    case 3:
                        i = 0;
                        progressDialog.setMessage("正在加载,请稍后...");
                        break;
                    default:
                }
            }
        }, 0, 800, TimeUnit.MILLISECONDS);

        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        progressDialog.dismiss();
        executorService.shutdownNow();
    }
}
