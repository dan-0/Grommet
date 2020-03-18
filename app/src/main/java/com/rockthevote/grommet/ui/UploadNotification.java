package com.rockthevote.grommet.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.RockyRequest;


/**
 * Helper class for showing and canceling upload
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class UploadNotification {

    private static final String NOTIFICATION_SUCCESS_TAG = "notification_success_tag";
    private static final String NOTIFICATION_FAILURE_TAG = "notification_failure_tag";

    private static final int NOTIFICATION_SUCCESS_ID = 200;
    private static final int NOTIFICATION_FAILURE_ID = 400;


    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of upload notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancelSuccess(Context) (Context)
     * @see #cancelFailure(Context) (Context)
     */
    public static void notify(final Context context, RockyRequest.Status status) {

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(0)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(context.getString(getNotifTitle(status)))
                .setContentText(context.getString(getContentText(status)))
                .setSmallIcon(R.drawable.ic_stat_upload)
                .setAutoCancel(false);

        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(getNotificationTag(status), getNotificationId(status), builder.build());
    }


    private static
    @StringRes
    int getNotifTitle(RockyRequest.Status status) {
        switch (status) {
            case REGISTER_SUCCESS:
                return R.string.upload_notification_title_success;
            case REGISTER_CLIENT_FAILURE:
                return R.string.upload_notification_content_client_failure;
            case REGISTER_SERVER_FAILURE:
            default:
                return R.string.upload_notification_title_failure;
        }
    }

    private static
    @StringRes
    int getContentText(RockyRequest.Status status) {
        switch (status) {
            case REGISTER_SUCCESS:
                return R.string.upload_notification_content_success;
            case REGISTER_CLIENT_FAILURE:
                return R.string.upload_notification_content_client_failure;
            case REGISTER_SERVER_FAILURE:
            default:
                return R.string.upload_notification_content_connection_failure;
        }
    }

    private static int getNotificationId(RockyRequest.Status status) {
        switch (status) {
            case REGISTER_SUCCESS:
                return NOTIFICATION_SUCCESS_ID;
            default:
            case REGISTER_SERVER_FAILURE:
                return NOTIFICATION_FAILURE_ID;
        }
    }

    private static String getNotificationTag(RockyRequest.Status status) {
        switch (status) {
            case REGISTER_SUCCESS:
                return NOTIFICATION_SUCCESS_TAG;
            default:
            case REGISTER_SERVER_FAILURE:
                return NOTIFICATION_FAILURE_TAG;
        }
    }

    /**
     * Cancels any notifications of this type previously shown
     */
    public static void cancelSuccess(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_SUCCESS_TAG, NOTIFICATION_SUCCESS_ID);

    }

    public static void cancelFailure(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_FAILURE_TAG, NOTIFICATION_FAILURE_ID);

    }
}
