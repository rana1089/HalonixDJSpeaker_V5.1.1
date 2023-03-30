package com.wansnn.csc.wsbulb;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wansnn.csc.wsbulb.jl_dialog.Jl_Dialog;
import com.wansnn.csc.wsbulb.jl_dialog.interfaces.OnViewClickListener;
import com.wansnn.csc.wsbulb.music.Constant;
import com.wansnn.csc.wsbulb.music.FormatHelper;
import com.wansnn.csc.wsbulb.music.MusicBroadCast;
import com.wansnn.csc.wsbulb.music.MusicLoader;
import com.wansnn.csc.wsbulb.music.PlayerService;

import java.util.List;

@SuppressLint("ResourceAsColor")
public class MusicActivity extends AppCompatActivity {

    private String TAG = "MusicActivity";
    public ImageButton btn_mode;//播放模式：循环，单次，顺序，随机
    public ImageButton btn_play_stop;//播放按钮：停止，播放
    public ListView music_listview;//音乐列表

    private List<MusicLoader.MusicInfo> musicList;

    int currentMusic, currentPosition;
    int mode = 3;
    Intent i;

    MusicAdapter adapter;

    MusicBroadCast musicBroadCast;

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    private Jl_Dialog notifyGpsDialog;
    private static Boolean isBluetoothDialogShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            Log.w(TAG, "isFmActive: couldn't get AudioManager reference");
        } else {
            if (audioManager.isMusicActive())
                Log.d(TAG, "播放器是OK的 ");
            else
                Log.d(TAG, "播放器是在使用的 ");
        }

        addBroadcastReceiver();
//		musicBroadCast = new MusicBroadCast(this);
//		musicBroadCast.setOnHeadsetListener(new MusicBroadCast.onHeadsetListener() {
//			@Override
//			public void receiverPlay() {
//				Log.i("~~~~~~", "receiverPlay: ~~~~~~~~~~~~~~~~~~");
//			}
//
//			@Override
//			public void receiverPause() {
//				Log.i("~~~~~~", "receiverPause: ~~~~~~~~~~~~~~~~~~");
//			}
//
//			@Override
//			public void playOrPause() {
//				Log.i("~~~~~~", "playOrPause: ~~~~~~~~~~~~~~~~~~");
//
//			}
//
//			@Override
//			public void playNext() {
//				Log.i("~~~~~~", "playNext: ~~~~~~~~~~~~~~~~~~");
//			}
//
//			@Override
//			public void playPrevious() {
//				Log.i("~~~~~~", "playPrevious: ~~~~~~~~~~~~~~~~~~");
//			}
//		});


        btn_play_stop = (ImageButton) findViewById(R.id.btn_play_stop);
        btn_mode = findViewById(R.id.btn_mode);

        MusicLoader musicLoader = MusicLoader.instance(getContentResolver());
        musicList = musicLoader.getMusicList();
        adapter = new MusicAdapter();
        music_listview = (ListView) findViewById(R.id.music_listview);
        music_listview.setAdapter(adapter);
        music_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                currentMusic = position;
                currentPosition = 0;
                i = new Intent(Constant.ACTION_MUSIC_CTRL_START);
                i.putExtra("position", currentMusic);
                i.putExtra("progress", currentPosition);
                sendBroadcast(i);
            }
        });

        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    // Pause playback
                    Log.e("VideoPreViewDialog", "====" + focusChange);
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    Log.e("VideoPreViewDialog", "失去音频焦点");
                    i = new Intent(Constant.ACTION_MUSIC_CTRL_PAUSE);
                    sendBroadcast(i);
                    audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    //获得焦点之后的操作
                    Log.e("VideoPreViewDialog", "获取音频焦点");
                } else {
                    Log.e("VideoPreViewDialog", "====" + focusChange);
                }
            }
        };
        if (!isBluetoothDialogShown) {
            showNotifyBluetoothDialog();
            isBluetoothDialogShown = true;
        }
    }

    /**
     * for displaying the bluetooth connection info dialog
     **/

    private void showNotifyBluetoothDialog() {
        if (notifyGpsDialog == null) {
            notifyGpsDialog = Jl_Dialog.builder()
                    .title(getString(R.string.connect_bluetooth_tip))
                    .left(getString(R.string.cancel))
                    .right(getString(R.string.connect))
                    .backgroundColor(Color.WHITE)
                    .titleColor(0xFF777777)
                    .leftClickListener(new OnViewClickListener() {
                        @Override
                        public void onClick(View v, DialogFragment dialogFragment) {
                            dismissNotifyBluetoothDialog();
                        }
                    })
                    .rightClickListener(new OnViewClickListener() {
                        @Override
                        public void onClick(View v, DialogFragment dialogFragment) {
                            dismissNotifyBluetoothDialog();
                            Intent intentOpenBluetoothSettings = new Intent();
                            intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intentOpenBluetoothSettings);
                        }
                    })
                    .build();
        }
        if (!notifyGpsDialog.isShow()) {
            notifyGpsDialog.show(getSupportFragmentManager(), "notify_bluetooth_dialog");
        }
    }

    /**
     * for dismissing the bluetooth connection info dialog
     **/

    private void dismissNotifyBluetoothDialog() {
        if (notifyGpsDialog != null) {
            if (notifyGpsDialog.isShow() && !isDestroyed()) {
                notifyGpsDialog.dismiss();
            }
            notifyGpsDialog = null;
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous:
                i = new Intent(Constant.ACTION_MUSIC_CTRL_PREVIOUS);
                sendBroadcast(i);
                break;
            case R.id.btn_play_stop:
                play(currentMusic);
                break;
            case R.id.btn_next:
                i = new Intent(Constant.ACTION_MUSIC_CTRL_NEXT);
                sendBroadcast(i);
                break;
            case R.id.btn_mode:
                mode++;
                if (mode > 3) {
                    mode = 0;
                }
                if (mode == 0) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.cyle_one));
                } else if (mode == 1) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.cycle));
                } else if (mode == 2) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.random));
                } else if (mode == 3) {//
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.abc));
                }
                i = new Intent(Constant.ACTION_MUSIC_CTRL_MODE);
                i.putExtra(Constant.ACTION_MUSIC_CTRL_MODE, mode);
                sendBroadcast(i);
                break;
            case R.id.button4:
                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        boolean isOK = requestTransientAudioFocus(true, onAudioFocusChangeListener);
        if (!isOK) Log.e(TAG, "play: =======没有获得焦点");
        else Log.e(TAG, "play: =======获得焦点");

        i = new Intent(Constant.ACTION_MUSIC_CTRL_GET_START);
        sendBroadcast(i);
    }

    private void play(int position) {

        if (music_state) {
//			natureBinder.stopPlay();
//			btn_play_stop.setBackgroundResource(R.drawable.stop);
            i = new Intent(Constant.ACTION_MUSIC_CTRL_PAUSE);
            sendBroadcast(i);
//			audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        } else {

//			natureBinder.startPlay(position,currentPosition);
//			btn_play_stop.setBackgroundResource(R.drawable.paly);
            i = new Intent(Constant.ACTION_MUSIC_CTRL_START);
            i.putExtra("position", currentMusic);
            i.putExtra("progress", currentPosition);
            sendBroadcast(i);
        }
    }

    private static final int STATE = 101;
    private static final int VIEW_STATE = 102;
    private  Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATE:

                    if (music_state) {
                        btn_play_stop.setBackground(getResources().getDrawable(R.drawable.paly));
//					MyApplication.getApplication().showButtonNotify("","");
                    } else {
                        btn_play_stop.setBackground(getResources().getDrawable(R.drawable.stop));
//					MyApplication.getApplication().hidenButtonNotify();
                    }
                    break;
                case VIEW_STATE:
                    adapter.setmSelected(currentMusic);
                    music_listview.smoothScrollToPosition(currentMusic);
                    break;
            }
        }
    };

    boolean music_state = false;
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
//			Log.e(TAG, "收到BroadcastReceiver :"+intent.getAction() );
            if (intent.getAction().equals(Constant.ACTION_UPDATE_STATE)) {
                music_state = intent.getBooleanExtra(Constant.ACTION_UPDATE_STATE, false);
                handler.sendEmptyMessage(STATE);
            } else if (intent.getAction().equals(Constant.ACTION_UPDATE_VIEW_STATE)) {
                currentMusic = intent.getIntExtra(Constant.ACTION_UPDATE_VIEW_STATE, 0);
                handler.sendEmptyMessage(VIEW_STATE);
            } else if (intent.getAction().equals(PlayerService.ACTION_UPDATE_PROGRESS)) {
                int progress = intent.getIntExtra(PlayerService.ACTION_UPDATE_PROGRESS, 0);
                if (progress > 0) {
                    currentPosition = progress; // Remember the current position
                }
            } else if (intent.getAction().equals(Constant.ACTION_UPDATE_MODE)) {
                int Mode = 0;
                if (intent.hasExtra(Constant.ACTION_MUSIC_CTRL_MODE)) {
                    Mode = intent.getIntExtra(Constant.ACTION_MUSIC_CTRL_MODE, 0);
                }
                mode = Mode;
                if (mode == 0) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.cyle_one));
                } else if (mode == 1) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.cycle));
                } else if (mode == 2) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.random));
                } else if (mode == 3) {
                    btn_mode.setBackground(getResources().getDrawable(R.drawable.abc));
                }
            }
        }
    };


    public void addBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();

        filter.addAction(Constant.ACTION_UPDATE_MODE);
        filter.addAction(Constant.ACTION_UPDATE_STATE);
        filter.addAction(Constant.ACTION_UPDATE_VIEW_STATE);
        filter.addAction(PlayerService.ACTION_UPDATE_PROGRESS);
        filter.addAction(PlayerService.ACTION_UPDATE_DURATION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
//		musicBroadCast.unregisterHeadsetReceiver(this);
    }


    class MusicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object getItem(int position) {
            return musicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return musicList.get(position).getId();
        }

        private int mSelected = -1;

        public int getmSelected() {
            return mSelected;
        }

        public void setmSelected(int mSelected) {
            this.mSelected = mSelected;
            notifyDataSetChanged();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
//				Log.e(TAG, "getView: ===========" );
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.music_item, null);
                ImageView pImageView = (ImageView) convertView.findViewById(R.id.albumPhoto);
                TextView pTitle = (TextView) convertView.findViewById(R.id.title);
                TextView pDuration = (TextView) convertView.findViewById(R.id.duration);
                TextView pArtist = (TextView) convertView.findViewById(R.id.artist);
                RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
                viewHolder = new ViewHolder(relativeLayout, pImageView, pTitle, pDuration, pArtist);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.imageView.setImageResource(R.drawable.music);
            viewHolder.title.setText(musicList.get(position).getTitle());
            viewHolder.duration.setText(FormatHelper.formatDuration(musicList.get(position).getDuration()));
            viewHolder.artist.setText(musicList.get(position).getArtist());
            if (mSelected >= 0 && position == mSelected) {
                convertView.setBackgroundColor(Color.parseColor("#50FFFFFF"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#00000000"));
            }
            return convertView;
        }

    }

    class ViewHolder {
        public ViewHolder(RelativeLayout mRelativeLayout, ImageView pImageView, TextView pTitle, TextView pDuration, TextView pArtist) {
            relativeLayout = mRelativeLayout;
            imageView = pImageView;
            title = pTitle;
            duration = pDuration;
            artist = pArtist;
        }

        RelativeLayout relativeLayout;
        ImageView imageView;
        TextView title;
        TextView duration;
        TextView artist;
    }

    /**
     * 获取永久的音频焦点
     *
     * @param request
     * @return
     */
    public boolean requestPermanentAudioFocus(boolean request,
                                              AudioManager.OnAudioFocusChangeListener listener) {
        if (request) {
            int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            int result = audioManager.abandonAudioFocus(listener);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    /**
     * 获取短暂的音频焦点
     *
     * @param request
     * @return
     */
    public boolean requestTransientAudioFocus(boolean request,
                                              AudioManager.OnAudioFocusChangeListener listener) {
        if (request) {
            int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            int result = audioManager.abandonAudioFocus(listener);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

    }
}
