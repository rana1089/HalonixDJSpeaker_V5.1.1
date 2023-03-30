package com.wansnn.csc.wsbulb;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wansnn.csc.wsbulb.new_music.LocalMusicUtils;
import com.wansnn.csc.wsbulb.new_music.PermissionUtil;
import com.wansnn.csc.wsbulb.new_music.PlayItemAdapter;
import com.wansnn.csc.wsbulb.new_music.Song;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class newMusicActivity extends AppCompatActivity {
    private RecyclerView playListView;

    private PlayItemAdapter playItem;
    private List<Song> songList;
    int playPostion;

    private ImageButton playBtn;
    private ImageButton previousBtn;
    private ImageButton nextBtn;

    //播放样式 随机 单曲
    private ImageButton playStyleBtn;
    private Boolean isPlaying;

    private boolean     isPermisson;
    private Boolean     isStop;

    private MediaPlayer mediaPlayer;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_music);

        playListView = (RecyclerView)findViewById(R.id.play_list_view);
        playListView.setLayoutManager(new LinearLayoutManager(this));
        playBtn =(ImageButton)findViewById(R.id.btn_play_stop);
        previousBtn =(ImageButton)findViewById(R.id.btn_previous);
        nextBtn =(ImageButton)findViewById(R.id.btn_next);
        playStyleBtn =findViewById(R.id.btn_mode);


        isPlaying = false;
        //播放 暂停
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPermisson){
                    return;
                }
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    playBtn.setBackground(getResources().getDrawable(R.drawable.stop));

                }else {
                    mediaPlayer.start();
                    playBtn.setBackground(getResources().getDrawable(R.drawable.paly));
                }
            }
        });
        //上一首
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPermisson){
                    return;
                }
                playPostion--;
                if (playPostion == -1) {
                    playPostion = 0;
                }
                changeCurrentPlayMusic();
                playMusic();
            }
        });
        //下一首
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPermisson){
                    return;
                }
                playPostion++;
                if (playPostion == songList.size()) {
                    playPostion = 0;
                }
                changeCurrentPlayMusic();
                playMusic();
            }
        });

        String permissionName = "android.permission.READ_EXTERNAL_STORAGE";
        isPermisson = PermissionUtil.hasPermission(this,permissionName);
        if (isPermisson){
            //加载本地的音乐文件
            this.loadLocalMusic();
            this.initMusic();
        }else {
            Toast.makeText(this,"没有权限",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 监听MediaPlayer.prepare()
     */
    private MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {

        }
    } ;

    /**
     * 监听播放结束的事件
     */
    private MediaPlayer.OnCompletionListener CompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (!isPermisson){
                return;
            }
            playPostion++;
            if (playPostion == songList.size()) {
                playPostion = 0;
            }
            changeCurrentPlayMusic();
            playMusic();
        }
    };

    public void initMusic(){
        if (mediaPlayer!=null){
            mediaPlayer.reset();
        }else {
            mediaPlayer =new MediaPlayer();
            mediaPlayer.setOnPreparedListener(PreparedListener);
            mediaPlayer.setOnCompletionListener(CompletionListener);
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (this.songList.size()==0){
            return;
        }

        Song song = this.songList.get(0);
        String musicPath = song.getPath();
        try {
            File f = new File(musicPath);
            if (f.exists()){
                FileInputStream fileInputStream = new FileInputStream(f);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                mediaPlayer.setDataSource(fileDescriptor);
                mediaPlayer.prepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playMusic(){
        if (mediaPlayer!=null){
            mediaPlayer.reset();
        }else {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(PreparedListener);
            mediaPlayer.setOnCompletionListener(CompletionListener);
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Song song = this.songList.get(playPostion);
        String musicPath = song.getPath();
        try {
            File f = new File(musicPath);
            if (f.exists()){
                FileInputStream fileInputStream = new FileInputStream(f);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                mediaPlayer.setDataSource(fileDescriptor);
                mediaPlayer.prepare();
                mediaPlayer.start();

                playBtn.setBackground(getResources().getDrawable(R.drawable.paly));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void loadLocalMusic(){
        List<Song> list= LocalMusicUtils.getmusic(this);
        this.songList = new ArrayList<Song>();
        for (int i=0;i<list.size();i++){
            Song song = list.get(i);
            Log.i("music~~~~~~~~~~", "loadLocalMusic: "+song.getPath());
            if (i==0){
                song.isPlaying = true;
                playPostion=0;
            }else {
                song.isPlaying = false;
            }
            this.songList.add(song);
        }

        playItem = new PlayItemAdapter(this,this.songList);
        playListView.setAdapter(playItem);
        playItem.setOnPlayItemClickListener(new PlayItemAdapter.OnPlayItemClickListener() {
            @Override
            public void onPlayItemClick(View v, Song song, int position) {
                String str = "名字"+song.getName()+"时长:"+song.getDuration();
                Log.i("music~~~~~~~~~~", "loadLocalMusic: "+str);
                playPostion=position;
                playMusic();
                isStop = false;
                changeCurrentPlayMusic();
                new Thread(new PlayThread()).start();//线程开始
            }
        });
    }

    public class PlayThread implements Runnable{

        @Override
        public void run() {
            while (mediaPlayer!=null&&isStop==false){
                handler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                try {
                    //100毫秒更新一次
                    Thread.sleep(80);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * 切换当前播放的音乐
     */
    public void changeCurrentPlayMusic(){
        Song song = this.songList.get(this.playPostion);
        List<Song> list = new ArrayList<Song>();
        for (int i=0;i<this.songList.size();i++){
            Song songObj = this.songList.get(i);
            songObj.isPlaying = false;
            if (songObj.id==song.id){
                songObj.isPlaying = true;
            }
            list.add(songObj);
        }
        this.songList = list;

        playItem.notifyDataSetChanged();
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.goBack:
                this.finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("MusicActivity", "onDestroy: " );
        super.onDestroy();
    }
}
