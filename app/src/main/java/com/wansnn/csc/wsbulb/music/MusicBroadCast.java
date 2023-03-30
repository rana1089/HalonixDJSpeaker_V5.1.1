package com.wansnn.csc.wsbulb.music;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

public class MusicBroadCast extends BroadcastReceiver {

    private Context context;
    private Timer timer = new Timer();
    private static int clickCount;

    private static onHeadsetListener headsetListener;

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    public MusicBroadCast(){
        super();
    }

    public MusicBroadCast(Context ctx){
        super();
        context = ctx;
        headsetListener = null;
        registerHeadsetReceiver(context);
    }

    public void registerHeadsetReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);// 另说context.AUDIO_SERVICE
        ComponentName name = new ComponentName(context.getPackageName(), MusicBroadCast.class.getName());// 另说MediaButtonReceiver.class.getName()
        audioManager.registerMediaButtonEventReceiver(name);
    }

    public void unregisterHeadsetReceiver(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(), MusicBroadCast.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(name);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获得Action
        String intentAction = intent.getAction();
        // 获得KeyEvent对象
//        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        String action = intent.getAction();
        Log.e("xxx","----------intent------------"+intent.getPackage());
        Log.e("xxx","----------onReceive------------"+action);
//        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
//            // 获得按键字节码
//            int keyCode = keyEvent.getKeyCode();
//            // 按下 / 松开 按钮
//            int keyAction = keyEvent.getAction();
//            // 获得事件的时间
//            long downtime = keyEvent.getEventTime();
//
//            // 获取按键码 keyCode
//            StringBuilder sb = new StringBuilder();
//            // 这些都是可能的按键码 ， 打印出来用户按下的键
//            if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
//                sb.append("KEYCODE_MEDIA_NEXT");
//            }
//            // 说明：当我们按下MEDIA_BUTTON中间按钮时，实际出发的是 KEYCODE_HEADSETHOOK 而不是
//            // KEYCODE_MEDIA_PLAY_PAUSE
//            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
//                sb.append("KEYCODE_MEDIA_PLAY_PAUSE");
//            }
//            if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
//                sb.append("KEYCODE_HEADSETHOOK");
//            }
//            if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
//                sb.append("KEYCODE_MEDIA_PREVIOUS");
//            }
//            if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
//                sb.append("KEYCODE_MEDIA_STOP");
//            }
//            // 输出点击的按键码
//            Log.i("~~~~~~~~~~~", sb.toString());
//        }


        if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)){
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
            //蓝牙断开
            if( bluetoothState== BluetoothAdapter.STATE_DISCONNECTED){
                handler.sendEmptyMessage(4);
            }
        }

        if(action.equals("android.intent.action.HEADSET_PLUG")){
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 2) == 0){
                    //拔出
                    handler.sendEmptyMessage(4);
                }else if (intent.getIntExtra("state", 2) == 1) {
                    //插入
                    handler.sendEmptyMessage(4);
                }
            }
        }
        if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                clickCount = clickCount + 1;
                if(clickCount == 1){
                    HeadsetTimerTask headsetTimerTask = new HeadsetTimerTask();
                    timer.schedule(headsetTimerTask,1000);
                }
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handler.sendEmptyMessage(2);
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handler.sendEmptyMessage(3);
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handler.sendEmptyMessage(4);
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handler.sendEmptyMessage(5);
            }
        }
    }

    class HeadsetTimerTask extends TimerTask {
        @Override
        public void run() {
            try{
                if(clickCount==1){
                    handler.sendEmptyMessage(1);
                }else if(clickCount==2){
                    handler.sendEmptyMessage(2);
                }else if(clickCount>=3){
                    handler.sendEmptyMessage(3);
                }
                clickCount=0;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 1) {
                    headsetListener.playOrPause();
                }else if(msg.what == 2){
                    headsetListener.playNext();
                }else if(msg.what == 3){
                    headsetListener.playPrevious();
                }else if(msg.what == 4){
                    headsetListener.receiverPause();
                } else if(msg.what == 5){
                    headsetListener.receiverPlay();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public interface onHeadsetListener{
        void receiverPlay();
        void receiverPause();
        void playOrPause();
        void playNext();
        void playPrevious();
    }

    public void setOnHeadsetListener(onHeadsetListener newHeadsetListener){
        headsetListener = newHeadsetListener;
    }



}
