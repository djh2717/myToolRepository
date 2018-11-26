package my.code.repository.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.widget.RemoteViews;

import my.demo.one.R;

/**
 * A notification util, can adaptation android 8.0
 *
 * @author Djh on 2018/7/23 09:14
 * @E-Mail 1544579459@qq.com
 */
@SuppressWarnings("ALL")
public class NotificationUtil {

    public static final int PRIORITY_MAX = NotificationCompat.PRIORITY_MAX;
    public static final int PRIORITY_DEFAULT = NotificationCompat.PRIORITY_DEFAULT;

    /**
     * Channel id, default use default channel id.
     */
    public static final String DEFAULT_CHANNEL_ID = "defaultChannelId";
    public static final String DOWN_LOAD_CHANNEL_ID = "downloadChannelId";
    public static final String MAX_IMPORTANCE_CHANNEL_ID = "maxImportanceChannelId";


    /**
     * Use to custom the content view and big content view.
     */
    private static RemoteViews sContentView;
    private static RemoteViews sBigContentView;

    /**
     * Send notification, this is usually use in demo.
     * Use default channel.
     */
    public static void sendNotification(Context context, Class pendingActivity, String content, int priority,
                                        int notificationId) {

        constructAndSend(context, pendingActivity, DEFAULT_CHANNEL_ID, -1, null, content, false, priority, notificationId, -1);
    }

    /**
     * This is use in formal APP, the notification have a pending, if you do not
     * need the progress, set it as -1.
     */
    public static void sendNotification(Context context, Class pendingActivity, String channelId, int iconId,
                                        @Nullable String contentTitle, String content, boolean needLargeIcon,
                                        int notificationPriority, int notificationId, int progress) {

        constructAndSend(context, pendingActivity, channelId, iconId, contentTitle, content, needLargeIcon, notificationPriority, notificationId, progress);
    }

    /**
     * This is use in formal APP, the notification have a pending, the channel id is
     * the {@link #DEFAULT_CHANNEL_ID}. If you do not need the progress, set it as -1.
     */
    public static void sendNotification(Context context, Class pendingActivity, int iconId,
                                        @Nullable String contentTitle, String content, boolean needLargeIcon,
                                        int notificationPriority, int notificationId, int progress) {

        constructAndSend(context, pendingActivity, DEFAULT_CHANNEL_ID, iconId, contentTitle, content, needLargeIcon, notificationPriority, notificationId, progress);
    }

    /**
     * This is use in formal APP, the notification have no pending, if you do not
     * need the progress, set it as -1.
     */
    public static void sendNotification(String channelId, int iconId, @Nullable String contentTitle,
                                        String content, boolean needLargeIcon, int notificationPriority,
                                        int notificationId, int progress) {

        constructAndSend(MyApplication.getContext(), null, channelId, iconId, contentTitle, content, needLargeIcon, notificationPriority, notificationId, progress);
    }

    /**
     * This is use in formal APP, the notification have no pending, use the {@link #DEFAULT_CHANNEL_ID}.
     * If you do not need the progress, set it as -1.
     */
    public static void sendNotification(int iconId, @Nullable String contentTitle,
                                        String content, boolean needLargeIcon, int notificationPriority,
                                        int notificationId, int progress) {

        constructAndSend(MyApplication.getContext(), null, DEFAULT_CHANNEL_ID, iconId, contentTitle, content, needLargeIcon, notificationPriority, notificationId, progress);
    }


    /**
     * Use to custom the content view, this is should call before the {@link #sendNotification}
     */
    public static void setContentView(RemoteViews contentView) {
        sContentView = contentView;
    }

    /**
     * Use to custom the big content view, this is should call before the {@link #sendNotification}
     */
    public static void setBigContentView(RemoteViews bigContentView) {
        sBigContentView = bigContentView;
    }

    /**
     * Use to cancel notification.
     */
    public static void cancelNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancel(id);
    }


    // ------------------ Internal API ------------------

    /**
     * Send notification.
     */
    private static void constructAndSend(Context context, Class activity, String channelId, int iconId,
                                         String contentTitle, String content, boolean needLargeIcon,
                                         int priority, int notificationId, int progress) {

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }
        NotificationCompat.Builder builder;

        //Adaptation 8.0 of notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (notificationManager.getNotificationChannel(channelId) == null) {
                createChannel(context, channelId, notificationManager);
            } else {
                //Check channel whether is close by djh.
                if (checkChannelIsClose(notificationManager, channelId)) {
                    return;
                }
            }
            //The difference is different builder.
            builder = new NotificationCompat.Builder(context, channelId);

        } else {
            //Under 8.0 of notification.
            builder = new NotificationCompat.Builder(context);
        }

        // Construct a notification.
        constructNotification(context, activity, iconId, contentTitle, content, needLargeIcon, priority, progress, builder);

        //Send notification.
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    private static void constructNotification(Context context, Class pendingActivity, int iconId,
                                              String contentTitle, String content, boolean needLargeIcon,
                                              int priority, int progress, NotificationCompat.Builder builder) {

        //Start setting notifications.

        if (pendingActivity != null) {
            Intent intent = new Intent(context, pendingActivity);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        builder.setAutoCancel(true);
        builder.setContentText(content);
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

        // Show the progress if need.
        if (progress != -1) {
            // This use to close the sound.
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
            builder.setProgress(100, progress, false);
        }

        // Set the custom content or big content view.
        if (sContentView != null) {
            // This remote view is at another process.
            builder.setCustomContentView(sContentView);
            sContentView = null;
        }
        if (sBigContentView != null) {
            // This remote view is at another process.
            builder.setCustomBigContentView(sBigContentView);
            sBigContentView = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void createChannel(Context context, String channelId, NotificationManager notificationManager) {

        NotificationChannel notificationChannel = null;

        //According to caller afferent priority,create channel initial state priority,
        //and set the channel name show to users. The priority can modify by djh in setting interface.
        // NOTICE: 2018/7/23 If you need add channel, add a static field and modify here.
        switch (channelId) {
            case MAX_IMPORTANCE_CHANNEL_ID:
                notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "悬浮通知", NotificationManager.IMPORTANCE_HIGH);
                break;
            case DEFAULT_CHANNEL_ID:
                notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "默认通知", NotificationManager.IMPORTANCE_DEFAULT);
                break;
            case DOWN_LOAD_CHANNEL_ID:
                notificationChannel = new NotificationChannel(channelId, context.getString(R.string.app_name) + "下载通知", NotificationManager.IMPORTANCE_DEFAULT);
                // This download channel default is no sound.
                notificationChannel.enableLights(false);
                notificationChannel.setSound(null, null);
                notificationChannel.enableVibration(false);
                break;
            default:
        }

        //Set the channel description.
        if (notificationChannel != null) {
            switch (channelId) {
                case MAX_IMPORTANCE_CHANNEL_ID:
                    notificationChannel.setDescription(context.getString(R.string.app_name) + "悬浮通知服务");
                    break;
                case DEFAULT_CHANNEL_ID:
                    notificationChannel.setDescription(context.getString(R.string.app_name) + "默认通知服务");
                    break;
                case DOWN_LOAD_CHANNEL_ID:
                    notificationChannel.setDescription(context.getString(R.string.app_name) + "下载通知服务");
                    break;
                default:
            }
        }

        if (notificationManager != null && notificationChannel != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Use to check the channel whether is close by djh. If close, show a toast.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static boolean checkChannelIsClose(NotificationManager notificationManager, String channelId) {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
        if (notificationChannel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            ToastUtil.showToast("通知已被关闭,请到设置打开");
            return true;
        }
        return false;
    }
}
