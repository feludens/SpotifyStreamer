package com.spadatech.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.models.Album;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AlbumsArrayAdapter extends ArrayAdapter<Album> {

    public Context context;

    public AlbumsArrayAdapter(Context context, ArrayList<Album> albums) {
        super(context, R.layout.list_item_albums, albums);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_albums, parent, false);
        }

        // Get the data item for this position
        Album album = getItem(position);

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.artist_name);
        ImageView imCover = (ImageView) convertView.findViewById(R.id.cover_picture);

        // Populate the data into the template view using the data object
        tvName.setText(album.getName());
        Picasso.with(context).load(album.getCoverImg()).placeholder(R.drawable.album_art_missing).into(imCover);

        // Return the completed view to render on screen
        return convertView;
    }

}