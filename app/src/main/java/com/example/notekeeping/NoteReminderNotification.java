package com.example.notekeeping;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static android.provider.Settings.System.getString;

/**
 * Helper class for showing and canceling note reminder
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class NoteReminderNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "NoteReminder";
    private static NotificationManager mNotificationManager;

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of note reminder notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void notify(final Context context, final String noteTitle,
                              final String noteText) {
        final Resources res = context.getResources();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder( context.getApplicationContext(), "notify_001" );
        Intent ii = new Intent( context.getApplicationContext(), MainActivity.class );
        PendingIntent pendingIntent = PendingIntent.getActivity( context, 0, ii, 0 );
        final Bitmap picture = BitmapFactory.decodeResource( res, R.drawable.logo );
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText( noteText ).setBigContentTitle( noteTitle ).setSummaryText( "Review Note" );
        mBuilder.setDefaults( Notification.DEFAULT_ALL );
        mBuilder.setContentIntent( pendingIntent );
        mBuilder.setSmallIcon( R.drawable.ic_stat_note_reminder );
        mBuilder.setContentTitle( "Review Note" );
        mBuilder.setContentText( noteText );
        mBuilder.setPriority( NotificationCompat.PRIORITY_DEFAULT );
        mBuilder.setLargeIcon( picture );
        mBuilder.setTicker( "Review Note" );
        mBuilder.setStyle( bigText );

        mNotificationManager =
                (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel title",
                    importance );
            mNotificationManager.createNotificationChannel( channel );
            mBuilder.setChannelId( channelId );
        }

        mNotificationManager.notify( 0, mBuilder.build() );
    }

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
       /* final Bitmap picture = BitmapFactory.decodeResource( res, R.drawable.example_picture );


        final String ticker = exampleString;
        final String title = res.getString(
                R.string.note_reminder_notification_title_template, exampleString );
        final String text = res.getString(
                R.string.note_reminder_notification_placeholder_text_template, exampleString );




        final NotificationCompat.Builder builder = new NotificationCompat.Builder( context )

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults( Notification.DEFAULT_ALL )
                .setSmallIcon( R.drawable.ic_stat_note_reminder )
                .setContentTitle( title )
                .setContentText( text ).setPriority( NotificationCompat.PRIORITY_DEFAULT )
                .setLargeIcon( picture )
                .setTicker( ticker )
                .setNumber( number )
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent( Intent.ACTION_VIEW, Uri.parse( "http://www.google.com" ) ),
                                PendingIntent.FLAG_UPDATE_CURRENT ) )
                .setAutoCancel( true );

        notify( context, builder.build() );
    }*/

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService( Context.NOTIFICATION_SERVICE );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify( NOTIFICATION_TAG, 0, notification );
        } else {
            nm.notify( NOTIFICATION_TAG.hashCode(), notification );
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService( Context.NOTIFICATION_SERVICE );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel( NOTIFICATION_TAG, 0 );
        } else {
            nm.cancel( NOTIFICATION_TAG.hashCode() );
        }
    }
}
