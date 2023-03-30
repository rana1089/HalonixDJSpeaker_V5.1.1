package com.wansnn.csc.wsbulb.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.wansnn.csc.wsbulb.NotificationUtil;
import com.wansnn.csc.wsbulb.R;

import java.util.List;

//import com.wansnn.csc.wsbulb.music.Constant;
//import com.wansnn.csc.wsbulb.music.MusicLoader;


public class PlayerService extends Service implements OnCompletionListener {

	private static final String TAG = "PlayerService";
	
	public static final String MUSICS = "com.example.nature.MUSIC_LIST";
	
	public static final String NATURE_SERVICE = "com.example.nature.NatureService";
	
	private MediaPlayer mediaPlayer;

	private boolean isPlaying = false;
	
	private List<MusicLoader.MusicInfo> musicList;

	private int currentMusic;
	private int currentPosition;
	
	private static final int updateProgress = 1;
	private static final int updateCurrentMusic = 2;
	private static final int updateDuration = 3;
	
	public static final String ACTION_UPDATE_PROGRESS = "com.example.nature.UPDATE_PROGRESS";
	public static final String ACTION_UPDATE_DURATION = "com.example.nature.UPDATE_DURATION";
	public static final String ACTION_UPDATE_CURRENT_MUSIC = "com.example.nature.UPDATE_CURRENT_MUSIC";
	
	private int currentMode = 3; //default sequence playing
	
	public static final String[] MODE_DESC = {"Single Loop", "List Loop", "Random", "Sequence"};
	
	public static final int MODE_ONE_LOOP = 0;
	public static final int MODE_ALL_LOOP = 1;
	public static final int MODE_RANDOM = 2;
	public static final int MODE_SEQUENCE = 3;
	
	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public int getCurrentMode() {
		return currentMode;
	}

	public void setCurrentMode(int currentMode) {
		this.currentMode = currentMode;
	}

	private void toUpdateProgress(){
		if(mediaPlayer != null && isPlaying){					
			int progress = mediaPlayer.getCurrentPosition();					
			Intent intent = new Intent();
			intent.setAction(ACTION_UPDATE_PROGRESS);
			intent.putExtra(ACTION_UPDATE_PROGRESS,progress);
			sendBroadcast(intent);
			handler.sendEmptyMessageDelayed(updateProgress, 1000);					
		}
	}
	private void toUpdateDuration(){
		if(mediaPlayer != null){					
			int duration = mediaPlayer.getDuration();					
			Intent intent = new Intent();
			intent.setAction(ACTION_UPDATE_DURATION);
			intent.putExtra(ACTION_UPDATE_DURATION,duration);
			sendBroadcast(intent);									
		}
	}
	
	private void toUpdateCurrentMusic(){
		Intent intent = new Intent();
		intent.setAction(ACTION_UPDATE_CURRENT_MUSIC);
		intent.putExtra(ACTION_UPDATE_CURRENT_MUSIC,currentMusic);
		sendBroadcast(intent);				
	}
	
	public void onCreate(){
		initMediaPlayer();
		musicList = MusicLoader.instance(getContentResolver()).getMusicList();
		super.onCreate();
		addBroadcastReceiver();
	}	
	
	public void onDestroy(){
		if(mediaPlayer != null){
			mediaPlayer.release();
			mediaPlayer = null;
		}
		unregisterReceiver(mReceiver);
	}	
	
	/**
	 * initialize the MediaPlayer
	 */
	private void initMediaPlayer(){
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.e(TAG, "++++++++++toUpdateProgress: "+ currentPosition);
				mediaPlayer.seekTo(currentPosition);
				//设置 MediaPlayer 的 OnSeekComplete 监听
				mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
					@Override
					public void onSeekComplete(MediaPlayer mp) {
						// seekTo 方法完成时的回调
						mediaPlayer.start();
//						setupVisualizer();

						Log.e(TAG, "[OnPreparedListener] Start at " + currentMusic + " in mode " + currentMode + ", currentPosition : " + currentPosition);
						handler.sendEmptyMessage(updateDuration);
					}
				});
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(isPlaying){
					Log.e(TAG, "[OnCompletionListener] On Completion at " + currentMusic);
					switch (currentMode) {
					case MODE_ONE_LOOP:
						Log.e(TAG, "[Mode] currentMode = MODE_ONE_LOOP.");
						mediaPlayer.start();
						break;					
					case MODE_ALL_LOOP:
						Log.e(TAG, "[Mode] currentMode = MODE_ALL_LOOP.");
						play((currentMusic + 1) % musicList.size(), 0);
						break;
					case MODE_RANDOM:
						Log.e(TAG, "[Mode] currentMode = MODE_RANDOM.");
						play(getRandomPosition(), 0);
						break;
					case MODE_SEQUENCE:
						Log.e(TAG, "[Mode] currentMode = MODE_SEQUENCE.");
						if(currentMusic < musicList.size() - 1){						
							playNext();
						}
						break;
					default:
						Log.e(TAG, "No Mode selected! How could that be ?");
						break;
					}
					Log.e(TAG, "[OnCompletionListener] Going to play at " + currentMusic);
				}
			}
		});
	}
	
	private void setCurrentMusic(int pCurrentMusic){
		currentMusic = pCurrentMusic;
		handler.sendEmptyMessage(updateCurrentMusic);
	}
	
	private int getRandomPosition(){
		int random = (int)(Math.random() * (musicList.size() - 1));
		return random;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void startMyOwnForeground(){
		String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
		String channelName = "My Background Service";
		NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
		chan.setLightColor(Color.BLUE);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		assert manager != null;
		manager.createNotificationChannel(chan);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
		Notification notification = notificationBuilder.setOngoing(true)
				.setSmallIcon(R.mipmap.notif_icon)
				.setContentTitle("App is running in background")
				.setPriority(NotificationManager.IMPORTANCE_MIN)
				.setCategory(Notification.CATEGORY_SERVICE)
				.build();
		startForeground(2, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)   // added by manmohan chauhan
			startMyOwnForeground();							//
		else												//
			startForeground(NotificationUtil.notifyId,NotificationUtil.createNotification(this,"","",isPlaying));

		return super.onStartCommand(intent, flags, startId);
	}

	private void play(int currentMusic, int pCurrentPosition) {
		currentPosition = pCurrentPosition;
		setCurrentMusic(currentMusic);
		mediaPlayer.reset();
		if(!(musicList.size()<=0)) {
			try {
				mediaPlayer.setDataSource(musicList.get(currentMusic).getUrl());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.e(TAG, "[Play] Start Preparing at " + currentMusic);
			isPlaying = true;
			mediaPlayer.prepareAsync();
			handler.sendEmptyMessage(updateProgress);

			Intent i = new Intent(Constant.ACTION_UPDATE_STATE);
			i.putExtra(Constant.ACTION_UPDATE_STATE, isPlaying);
			sendBroadcast(i);

			i = new Intent(Constant.ACTION_UPDATE_VIEW_STATE);
			i.putExtra(Constant.ACTION_UPDATE_VIEW_STATE, currentMusic);
			sendBroadcast(i);
		}
		else{
			Toast.makeText(this, "No songs to Play.", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void stop(){
		mediaPlayer.stop();
		isPlaying = false;
		Intent i = new Intent(Constant.ACTION_UPDATE_STATE);
		i.putExtra(Constant.ACTION_UPDATE_STATE, isPlaying);
		sendBroadcast(i);
	}
	
	
	private void playNext(){
		switch(currentMode){
		case MODE_ONE_LOOP:
			play(currentMusic, 0);
			break;
		case MODE_ALL_LOOP:
			if(currentMusic + 1 == musicList.size()){
				play(0,0);
			}else{
				play(currentMusic + 1, 0);
			}
			break;
		case MODE_SEQUENCE:
			if(currentMusic + 1 == musicList.size()){
				Toast.makeText(this, "No more song.", Toast.LENGTH_SHORT).show();
			}else{
				play(currentMusic + 1, 0);
			}
			break;
		case MODE_RANDOM:
			play(getRandomPosition(), 0);
			break;
		}
	}
	
	private void playPrevious(){		
		switch(currentMode){
		case MODE_ONE_LOOP:
			play(currentMusic, 0);
			break;
		case MODE_ALL_LOOP:
			if(currentMusic - 1 < 0){
				play(musicList.size() - 1, 0);
			}else{
				play(currentMusic - 1, 0);
			}
			break;
		case MODE_SEQUENCE:
			if(currentMusic - 1 < 0){
				Toast.makeText(this, "No previous song.", Toast.LENGTH_SHORT).show();
			}else{
				play(currentMusic - 1, 0);
			}
			break;
		case MODE_RANDOM:
			play(getRandomPosition(), 0);
			break;
		}
	}
	private static final int PLAY = 4;
	private static final int NEXT = 5;
	private static final int PREVIOUS = 6;
	private static final int PAUSE = 7;
	private static final int SEND_START = 8;
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg){
			switch(msg.what){
			case PLAY:			
				currentMusic=msg.arg1;
				int progress=msg.arg2;
				play(currentMusic, progress);
				break;
			case NEXT:				
				playNext();
				break;
			case PAUSE:				
				stop();
				break;
			case PREVIOUS:				
				playPrevious();
				break;
			case updateProgress:				
				toUpdateProgress();
				break;
			case updateDuration:				
				toUpdateDuration();
				break;
			case updateCurrentMusic:
				toUpdateCurrentMusic();
				break;
			case SEND_START:
				Intent i = new Intent(Constant.ACTION_UPDATE_STATE);
				i.putExtra(Constant.ACTION_UPDATE_STATE, isPlaying);
				i.putExtra("currentMusic", currentMusic);
				sendBroadcast(i);
				
				Intent ib = new Intent(Constant.ACTION_UPDATE_VIEW_STATE);
				ib.putExtra(Constant.ACTION_UPDATE_VIEW_STATE, currentMusic);
				sendBroadcast(ib);
				
				Intent mob = new Intent(Constant.ACTION_UPDATE_MODE);
				mob.putExtra(Constant.ACTION_UPDATE_MODE, currentMode);
				sendBroadcast(mob);

				toUpdateDuration();
				break;
			}
		}
	};
	public final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Constant.ACTION_MUSIC_CTRL_START)) {
				
				int position=intent.getIntExtra("position", -1);
				int progress=intent.getIntExtra("progress", 0);
				if (position!=-1) {
					Message msg=new Message();
					msg.what=PLAY;
					msg.arg1=position;
					msg.arg2=progress;
					handler.sendMessage(msg);
				}
			}else if (intent.getAction().equals(Constant.ACTION_MUSIC_CTRL_PAUSE)) {
				handler.sendEmptyMessage(PAUSE);
			}else if (intent.getAction().equals(Constant.ACTION_MUSIC_CTRL_PREVIOUS)) {
				handler.sendEmptyMessage(PREVIOUS);
			}else if (intent.getAction().equals(Constant.ACTION_MUSIC_CTRL_NEXT)) {
				handler.sendEmptyMessage(NEXT);
			}else if (intent.getAction().equals(Constant.ACTION_MUSIC_CTRL_MODE)) {
				int mode=intent.getIntExtra(Constant.ACTION_MUSIC_CTRL_MODE, -1);
				if (mode!=-1) {
					currentMode=mode;
				}
			}else if (intent.getAction().equals(Constant.ACTION_MUSIC_CTRL_GET_START)) {
				
				handler.sendEmptyMessage(SEND_START);
			}
			
		}
	};
	
	public void addBroadcastReceiver() {
		Log.e(TAG, "addBroadcastReceiver: 增加音乐通知" );
		IntentFilter filter = new IntentFilter();

		filter.addAction(Constant.ACTION_MUSIC_CTRL_GET_START);
		filter.addAction(Constant.ACTION_MUSIC_CTRL_START);
		filter.addAction(Constant.ACTION_MUSIC_CTRL_PAUSE);
		filter.addAction(Constant.ACTION_MUSIC_CTRL_PREVIOUS);
		filter.addAction(Constant.ACTION_MUSIC_CTRL_NEXT);
		filter.addAction(Constant.ACTION_MUSIC_CTRL_MODE);


		registerReceiver(mReceiver, filter);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}	
	
		

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}


}

