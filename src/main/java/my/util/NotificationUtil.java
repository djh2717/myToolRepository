package my.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import advanced.demo.R;

/**
 * A notification util, can adaptation android 8.0
 *
 * @author Djh on 2018/7/23 09:14
 * E-Mail ：1544579459@qq.com
 */
public class NotificationUtil {
    public static final int PRIORITY_MAX = NotificationCompat.PRIORITY_MAX;
    public static final int PRIORITY_DEFAULT = NotificationCompat.PRIORITY_DEFAULT;

    /**
     * Channel id, default use main channel.
     */
    public static final String SUB_CHANNEL_ID = "subChannelId";
    public static final String MAIN_CHANNEL_ID = "mainChannelId";

    /**
     * Send notification, this is usually use in demo.
     * Use main channel.
     */
    public static void sendNotification(Context context, Class activity, String content, int priority, int notificationId) {
        send(context, activity, MAIN_CHANNEL_ID, -1, null, content, false, priority, notificationId);
    }

    /**
     * This is use in formal APP.
     */
    public static void sendNotification(Context context, Class activity, String channelId, int iconId, @Nullable String contentTitle, String content, boolean needLargeIcon, int priority, int notificationId) {
        send(context, activity, channelId, iconId, contentTitle, content, needLargeIcon, priority, notificationId);
    }

    /**
     * Send notification.
     */
    private static void send(Context context, Class activity, String channelId, int iconId, String contentTitle, String content, boolean needLargeIcon, int priority, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        //Adaptation 8.0 of notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel;
            //According to caller afferent priority,create channel initial state priority,
            //and set the channel name show to users. The priority can modify by user in setting interface.
            if (priority == PRIORITY_DEFAULT) {
                // FIXME: 2018/7/23 If you need add channel, add a static field and modify here.
                if (channelId.equals(MAIN_CHANNEL_ID)) {
                    notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "主要通知", NotificationManager.IMPORTANCE_DEFAULT);
                } else {
                    notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "次要通知", NotificationManager.IMPORTANCE_DEFAULT);
                }
            } else {
                // FIXME: 2018/7/23 Also need modify here like top.
                if (channelId.equals(MAIN_CHANNEL_ID)) {
                    notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "主要通知", NotificationManager.IMPORTANCE_HIGH);
                } else {
                    notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "次要通知", NotificationManager.IMPORTANCE_HIGH);
                }
            }
            //Set the channel description.
            if (channelId.equals(MAIN_CHANNEL_ID)) {
                notificationChannel.setDescription(context.getString(R.string.app_name) + "的主要通知服务");
            } else {
                notificationChannel.setDescription(context.getString(R.string.app_name) + "的次要通知服务");
            }

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
                //Check channel whether is close by user.
                checkChannel(notificationManager, channelId);
            }

            //The difference is different builder.
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            //Under 8.0 of notification.
            builder = new NotificationCompat.Builder(context);
        }

        //Start setting and send notifications.
        Intent intent = new Intent(context, activity);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setAutoCancel(true);
        builder.setContentText(content);
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        //Set the level at which notifications are displayed.
        //PUBLIC indicates that it will be displayed at any time.
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (contentTitle != null) {
            builder.setContentTitle(contentTitle);
        }

        if (priority == PRIORITY_DEFAULT) {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        if (needLargeIcon) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconId));
        }

        if (iconId != -1) {
            builder.setSmallIcon(iconId);
        } else {
            //This is usually use in demo.
            builder.setSmallIcon(android.R.drawable.ic_menu_my_calendar);
        }

        //Send notification.
        Notification notification = builder.build();
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
    }

    /**
     * Use to check the channel whether is close by user. If close, show a toast.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static void checkChannel(NotificationManager notificationManager, String channelId) {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
        if (notificationChannel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            ToastUtil.showToast("通知已被关闭,请到设置打开");
        }
    }
}
