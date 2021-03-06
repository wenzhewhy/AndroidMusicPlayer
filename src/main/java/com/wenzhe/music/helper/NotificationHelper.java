

package com.wenzhe.music.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.wenzhe.music.MusicActivity;
import com.wenzhe.music.PlayService;
import com.wenzhe.music.R;
import com.wenzhe.music.constants.PlayState;
import com.wenzhe.music.data.MusicInfo;
import com.wenzhe.music.utils.MediaUtils;


/**
 * Created by wenzhe on 2016/5/8.
 */
public class NotificationHelper {

    private Context context;
    private NotificationManager manager;

    private final int REQUEST_CODE = 0x100;
    private static final int NOTIFICATION_ID = 0x1000;

    public final static String NOTIFICATION_PREVIOUS = "notification_previous";
    public final static String NOTIFICATION_PLAY = "notification_play";
    public final static String NOTIFICATION_NEXT = "notification_next";
    public final static String NOTIFICATION_ACTIVITY = "notification_activity";

    private Bitmap targetBitmap;
    private int width;

    //private long currentMusicId = -1;

    private PendingIntent playIntent, previousIntent, nextIntent, activityIntent;

    public NotificationHelper(Context context) {
        this.context = context;

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        /*playIntent = PendingIntent.getService(context, REQUEST_CODE, new Intent(NOTIFICATION_PLAY)
                .setClass(context, PlayService.class), PendingIntent.FLAG_CANCEL_CURRENT);

        previousIntent = PendingIntent.getService(context, REQUEST_CODE, new Intent
                (NOTIFICATION_PREVIOUS)
                .setClass(context, PlayService.class), PendingIntent.FLAG_CANCEL_CURRENT);

        nextIntent = PendingIntent.getService(context, REQUEST_CODE, new Intent(NOTIFICATION_NEXT)
                .setClass(context, PlayService.class), PendingIntent.FLAG_CANCEL_CURRENT);*/

        activityIntent = PendingIntent.getActivity(context, REQUEST_CODE, new Intent
                (NOTIFICATION_ACTIVITY)
                .setClass(context, MusicActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        playIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(NOTIFICATION_PLAY), PendingIntent.FLAG_CANCEL_CURRENT);

        previousIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(NOTIFICATION_PREVIOUS), PendingIntent.FLAG_CANCEL_CURRENT);

        nextIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(NOTIFICATION_NEXT), PendingIntent.FLAG_CANCEL_CURRENT);

        /*activityIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(NOTIFICATION_ACTIVITY), PendingIntent.FLAG_CANCEL_CURRENT);*/

        manager.cancelAll();

        width = context.getResources().getDimensionPixelSize(R.dimen.notification_big);
    }

    private Notification.Builder createNotification() {
        //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap
        // .ic_launcher);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_icon_notification);
        builder.setAutoCancel(true);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        return builder;
    }

    public void show(MusicInfo info, String state) {
        //managerCompat.notify(NOTIFICATION_ID, createNotification(info));
        new MyTask(createNotification(), info, state).execute(info.getAlbumId());
    }

    private class MyTask extends AsyncTask<Long, Void, Bitmap> {
        private Notification.Builder builder;
        private MusicInfo info;
        private RemoteViews remoteViews, bigRemoteViews;
        private String state;

        public MyTask(Notification.Builder builder, MusicInfo info, String state) {
            this.info = info;
            this.builder = builder;
            this.state = state;
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            MediaUtils.getArtworkFromFile(context, params[0], options, true);
            if (targetBitmap == null) {
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inScaled = true;
                options.inDensity = options.outWidth;
                options.inTargetDensity = width;
                targetBitmap = MediaUtils.getArtworkFromFile(context, params[0], options, false);
            } else {
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inBitmap = targetBitmap;
                targetBitmap = MediaUtils.getArtworkFromFile(context, params[0], options, false);
            }
                /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;*/
            return targetBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            setRemoteViews(bitmap, state);
            Notification notification = builder.build();
            notification.bigContentView = bigRemoteViews;
            notification.contentView = remoteViews;
            manager.notify(NOTIFICATION_ID, notification);
        }

        private void setRemoteViews(Bitmap bitmap,String state) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout
                    .notification_white_layout);
            bigRemoteViews = new RemoteViews(context.getPackageName(), R.layout
                    .notification_white_layout_big);
            remoteViews.setTextViewText(R.id.notification_title, info.getTitle());
            remoteViews.setTextViewText(R.id.notification_artist, info.getArtist());

            bigRemoteViews.setTextViewText(R.id.notification_title_big, info.getTitle());
            bigRemoteViews.setTextViewText(R.id.notification_artist_big, info.getArtist());

            remoteViews.setOnClickPendingIntent(R.id.notification_previous, previousIntent);
            remoteViews.setOnClickPendingIntent(R.id.notification_play, playIntent);
            remoteViews.setOnClickPendingIntent(R.id.notification_next, nextIntent);
            remoteViews.setOnClickPendingIntent(R.id.notification_container, activityIntent);

            bigRemoteViews.setOnClickPendingIntent(R.id.notification_previous_big, previousIntent);
            bigRemoteViews.setOnClickPendingIntent(R.id.notification_play_big, playIntent);
            bigRemoteViews.setOnClickPendingIntent(R.id.notification_next_big, nextIntent);
            bigRemoteViews.setOnClickPendingIntent(R.id.notification_container_big,activityIntent);

            if (state == PlayHelper.STATE_PAUSE) {
                remoteViews.setImageViewResource(R.id.notification_play, R.mipmap
                        .widget_play_normal);
                bigRemoteViews.setImageViewResource(R.id.notification_play_big, R.mipmap
                        .widget_play_normal);
            } else {
                remoteViews.setImageViewResource(R.id.notification_play, R.mipmap
                        .widget_pause_normal);
                bigRemoteViews.setImageViewResource(R.id.notification_play_big, R.mipmap
                        .widget_pause_normal);
            }

            remoteViews.setImageViewBitmap(R.id.notification_img, bitmap);
            bigRemoteViews.setImageViewBitmap(R.id.notification_img_big, bitmap);
        }
    }

    public void cancel() {
        if (manager != null) {
            manager.cancelAll();
        }
    }


}
