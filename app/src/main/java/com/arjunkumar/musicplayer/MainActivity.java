package com.arjunkumar.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    ArrayList<Song> songsList;
    ListView listView;
    SongListAdapter adapter;

    private MusicService musicService;
    private Intent intent;
    private boolean musicBound = false;

    private MusicController musicController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.song_list);
        songsList = new ArrayList<>();

        getSongList();

        //sorting the song list by title
        Collections.sort(songsList, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        adapter = new SongListAdapter(this, songsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicService.setSong(position);
                musicService.playSong();
            }
        });

        setCotroller();
    }


    public void getSongList(){
        //retrieve song info

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor != null && musicCursor.moveToFirst()){

            //Retrieving the column index for data item
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);

            //adding songs to list
            do {

                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                songsList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());

        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;

            //getting service
            musicService = binder.getService();

            //passing list
            musicService.setList(songsList);
            musicBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;

        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if(intent == null){
            intent = new Intent(this, MusicService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    //menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_shuffle:
                break;
            case R.id.action_end:
                stopService(intent);
                musicService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(intent);
        musicService = null;
        super.onDestroy();
    }

    private void setCotroller(){
        musicController = new MusicController(this);

        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        musicController.setMediaPlayer(new MediaController.MediaPlayerControl() {
            @Override
            public void start() {

                musicService.go();
            }

            @Override
            public void pause() {
                musicService.pausePlayer();

            }

            @Override
            public int getDuration() {
                if(musicService != null && musicBound && musicService.isPlaying())
                    return musicService.getDuration();
                else
                return 0;
            }

            @Override
            public int getCurrentPosition() {
                if(musicService != null && musicBound && musicService.isPlaying())
                return musicService.getPosition();
                else return 0;
            }

            @Override
            public void seekTo(int pos) {
                musicService.seekTo(pos);

            }

            @Override
            public boolean isPlaying() {
                if(musicService != null && musicBound)
                    return musicService.isPlaying();
                else
                return false;
            }

            @Override
            public int getBufferPercentage() {
                return 0;
            }

            @Override
            public boolean canPause() {
                return true;
            }

            @Override
            public boolean canSeekBackward() {
                return true;
            }

            @Override
            public boolean canSeekForward() {
                return true;
            }

            @Override
            public int getAudioSessionId() {
                return 0;
            }
        });

        musicController.setAnchorView(findViewById(R.id.song_list));
        musicController.setEnabled(true);


    }

    private void playNext(){
        musicService.playNext();
        musicController.show();
    }

    private void playPrev(){
        musicService.playPrev();
        musicController.show();
    }
}
