package com.arjunkumar.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore.Audio.Media;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Arjun Kumar on 21-06-2016.
 */
public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songArrayList;
    private int songPostion;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    private final IBinder musicBind = new MusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();

        return false;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //initializing position
        songPostion = 0;

        //creating media player
        mediaPlayer = new MediaPlayer();

        //initializing music player
        initMusicPlayer();

    }

    public void initMusicPlayer(){
        //setting mediaPlayer properties
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //setting the listeners
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder builder = new Notification.Builder(getApplicationContext());

                builder.setContentIntent(pendingIntent)
                       .setSmallIcon(R.drawable.android_music_player_play)
                       .setTicker(songTitle)
                        .setOngoing(true)
                        .setContentTitle("Playing")
                        .setContentText(songTitle);

                Notification notification = builder.build();

                startForeground(NOTIFY_ID, notification);

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void setList(ArrayList<Song> songs){
        songArrayList = songs;

    }

    public class MusicBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    public void playSong(){
        mediaPlayer.reset();

        Song playSong = songArrayList.get(songPostion);
        long currentSong = playSong.getId();

        songTitle = playSong.getTitle();

        Uri trackUri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, currentSong);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.prepareAsync();
    }

    public void setSong(int songIndex){

        songPostion = songIndex;
    }

    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
    }

    public void seekTo(int pos){
        mediaPlayer.seekTo(pos);
    }

    public void go(){
        mediaPlayer.start();
    }

    //skip to  previous song
    public void playPrev(){
        songPostion--;
        if(songPostion < 0){
            songPostion = songArrayList.size() -1;
        }
        playSong();
    }

    //skip to next song
    public void playNext(){
        songPostion++;
        if(songPostion >= songArrayList.size()){
            songPostion = 0;
        }
        playSong();
    }


    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
