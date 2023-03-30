package com.wansnn.csc.wsbulb.new_music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.wansnn.csc.wsbulb.R;

import java.util.List;

public class PlayItemAdapter extends RecyclerView.Adapter {
    private List<Song> data;
    private Context context;
    private OnPlayItemClickListener onPlayItemClickListener=null;

    public void setOnPlayItemClickListener(OnPlayItemClickListener onPlayItemClickListener) {
        this.onPlayItemClickListener = onPlayItemClickListener;
    }

    public interface OnPlayItemClickListener{
        void onPlayItemClick(View v, Song song, int position);
    }
    public PlayItemAdapter(Context context, List<Song> list){
        this.data = list;
        context = context;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);

        PlayItemView itemView = new PlayItemView(inflater);
        return itemView;
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlayItemView itemView =(PlayItemView)holder;


        Song song = this.data.get(position);
        itemView.albumPhoto.setImageResource(R.drawable.music);
        itemView.songNameTV.setText(song.getName());
        itemView.singerTV.setText(song.getSinger());
        itemView.numTV.setText(formatTime(song.getDuration()));


        if (song.isPlaying){
            Log.i("song~~~~~~","播放");
//            itemView.songNameTV.setTextColor(Color.parseColor("#448EEE"));
//            itemView.singerTV.setTextColor(R.color.playingFontColor);
            itemView.background.setBackgroundColor(Color.parseColor("#50FFFFFF"));
        }else {
            Log.i("song~~~~~~","meiyou~~播放");
//            itemView.songNameTV.setTextColor(Color.parseColor("#666666"));
//            itemView.singerTV.setTextColor(R.color.playNormalFontColor);
            itemView.background.setBackgroundColor(Color.parseColor("#00000000"));
        }
    }

    //    转换歌曲时间的格式
    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            String tt = time / 1000 / 60 + ":0" + time / 1000 % 60;
            return tt;
        } else {
            String tt = time / 1000 / 60 + ":" + time / 1000 % 60;
            return tt;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull List payloads) {
        super.onBindViewHolder(holder, position, payloads);
        final Song song = this.data.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPlayItemClickListener !=null){
                    onPlayItemClickListener.onPlayItemClick(v,song,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }
    public class PlayItemView extends RecyclerView.ViewHolder{
        public ImageView albumPhoto;
        public TextView songNameTV;
        public TextView singerTV;
        public TextView numTV;
        public View background;

        public PlayItemView(@NonNull View itemView) {
            super(itemView);
            albumPhoto = itemView.findViewById(R.id.albumPhoto);
            songNameTV = (TextView)itemView.findViewById(R.id.title);
            singerTV = (TextView)itemView.findViewById(R.id.duration);
            numTV = (TextView)itemView.findViewById(R.id.artist);
            background = itemView;
        }
    }
}
