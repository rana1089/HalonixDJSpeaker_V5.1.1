package com.wansnn.csc.wsbulb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationUtil {

    private static RemoteViews mRemoteViews;
    /**
     * Notification 的ID
     */
    public final static int notifyId = 101;
    /**
     * NotificationCompat 构造器
     */
    public static NotificationCompat.Builder mBuilder;

    /**
     * 通知栏按钮点击事件对应的ACTION
     */
    public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";

    // 标识
    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 上一首 按钮点击 ID
     */
    public final static int BUTTON_PREV_ID = 1;
    /**
     * 播放/暂停 按钮点击 ID
     */
    public final static int BUTTON_PALY_ID = 2;
    /**
     * 下一首 按钮点击 ID
     */
    public final static int BUTTON_NEXT_ID = 3;

    public static NotificationManager mNotificationManager;

    public static String channelId = "mu-te",channelName = "";

    /**
     * 带按钮的通知栏
     */
    public static void showButtonNotify(Context mContext, String name, String author,boolean isPlay) {

       // mNotificationManager.notify(notifyId, createNotification(mContext,name,author,isPlay));
    }

    public static Notification createNotification(Context mContext, String name, String author,boolean isPlay){
        mBuilder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.app_name));
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
        mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.drawable.music);
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer, null == name || name.length() == 0 ? "歌名" : name);
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, author == null || author.length() == 0 ? "作家" : author);
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        // API3.0 以上的时候显示按钮，否则消失
        // 如果版本号低于（3。0），那么不显示按钮
        if (android.os.Build.VERSION.SDK_INT <= 9) {
            mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.GONE);
        } else {
            mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);
            if (isPlay) {
                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.stop);
            } else {
                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.paly);
            }
        }

        // 点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
        // 这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(mContext, 1, buttonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, intent_prev);
        /* 播放/暂停 按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(mContext, 2, buttonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_play, intent_paly);
        /* 下一首 按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(mContext, 3, buttonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);

        mBuilder.setContent(mRemoteViews).setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("正在播放").setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true).setSmallIcon(R.drawable.music);

        if(!channelId.isEmpty()) mBuilder.setChannelId(channelId);

        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        return notify;
    }
    public static void hideButtonNotify(Context mContext) {
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        mNotificationManager.cancel(notifyId);
    }

}
