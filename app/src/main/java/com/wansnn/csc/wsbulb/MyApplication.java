package com.wansnn.csc.wsbulb;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.wansnn.csc.wsbulb.music.MusicLoader;
import com.wansnn.csc.wsbulb.music.PlayerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csc on 2017/11/7.
 */

public class MyApplication extends Application {

    private static String TAG = "MyApplication";
    public static MyApplication application;
    private Intent localservice;
    public List<MusicLoader.MusicInfo> musicList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }



    public static MyApplication getApplication() {
        return application;
    }

    public void init() {

        Log.e(TAG, "init: 音乐服务启动");
        localservice = new Intent(this, PlayerService.class);
        this.startService(localservice);

//        // 注册广播
//        bReceiver = new ButtonBroadcastReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ACTION_BUTTON);
//        registerReceiver(bReceiver, intentFilter);
    }

//     /**
//     * 通知栏按钮广播
//     */
//    public ButtonBroadcastReceiver bReceiver;
//
//
//
//    private boolean isPlay = false;
//
//    int currentMusic, currentPosition;
//
//
//
//    /**
//     * 广播监听按钮点击事件
//     */
//    public class ButtonBroadcastReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO Auto-generated method stub
//            String action = intent.getAction();
//            if (action.equals(ACTION_BUTTON)) {
//                // 通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
//                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
//                switch (buttonId) {
//                    case BUTTON_PREV_ID:
//                        Log.d(TAG, "上一首");
//                        Intent pre = new Intent(Constant.ACTION_MUSIC_CTRL_PREVIOUS);
//                        sendBroadcast(pre);
////                        Toast.makeText(MainActivity.this, "上一首", Toast.LENGTH_SHORT).show();
//                        break;
//                    case BUTTON_PALY_ID:
//                        if (isPlay) {
//                            Intent stop = new Intent(Constant.ACTION_MUSIC_CTRL_PAUSE);
//                            sendBroadcast(stop);
//                            Log.e("tag", "notifi>>>>>>>设置为暂停");
//                        } else {
//                            Intent start = new Intent(Constant.ACTION_MUSIC_CTRL_START);
//                            start.putExtra("position", currentMusic);
//                            start.putExtra("progress", currentPosition);
//                            sendBroadcast(start);
//                            Log.e("tag", "notifi>>>>>>>设置为播放");
//                        }
//                        isPlay = !isPlay;
////                        showButtonNotify("","");
//                        break;
//                    case BUTTON_NEXT_ID:
//                        Intent next = new Intent(Constant.ACTION_MUSIC_CTRL_NEXT);
//                        sendBroadcast(next);
//                        Log.d(TAG, "下一首");
////                        Toast.makeText(MainActivity.this, "下一首", Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        break;
//                }
//            } else if (action.equals(Constant.ACTION_UPDATE_VIEW_STATE)) {
//                currentMusic = intent.getIntExtra(Constant.ACTION_UPDATE_VIEW_STATE, 0);
//            } else if (action.equals(PlayerService.ACTION_UPDATE_PROGRESS)) {
//                int progress = intent.getIntExtra(PlayerService.ACTION_UPDATE_PROGRESS, 0);
//                if (progress > 0) {
//                    currentPosition = progress; // Remember the current position
//                }
//            }
//        }
//    }


}
