package com.arjunkumar.musicplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjun Kumar on 17-06-2016.
 */
public class SongListAdapter extends ArrayAdapter<Song> {

    Context context;
    ArrayList<Song> songs;

    public SongListAdapter(Context context,ArrayList<Song> objects) {
        super(context, 0, objects);

        this.context = context;
        songs = objects;
    }

    public class ViewHolder{
        TextView title, artist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = View.inflate(context, R.layout.song_list_layout, null);
            ViewHolder vh = new ViewHolder();
            vh.title = (TextView) convertView.findViewById(R.id.song_title);
            vh.artist = (TextView) convertView.findViewById(R.id.song_artist);

            convertView.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();
        Song currentSong = getItem(position);

        vh.title.setText(currentSong.getTitle());
        vh.artist.setText(currentSong.getArtist());

        return convertView;
    }
}
