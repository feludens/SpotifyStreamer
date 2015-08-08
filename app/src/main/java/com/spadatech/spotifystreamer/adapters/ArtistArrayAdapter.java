package com.spadatech.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.models.ArtistResults;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ArtistArrayAdapter extends ArrayAdapter<ArtistResults> {

    public Context context;

    public ArtistArrayAdapter(Context context, ArrayList<ArtistResults> albums) {
        super(context, 0, albums);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ArtistResults album = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist_search, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.artistName);
        ImageView imCover = (ImageView) convertView.findViewById(R.id.coverPicture);
        // Populate the data into the template view using the data object
        tvName.setText(album.getName());
        //imCover.setImageBitmap(album.getAlbumCover());
        Picasso.with(context).load(album.getCoverImg()).placeholder(R.drawable.album_art_missing).into(imCover);
        // Return the completed view to render on screen
        return convertView;
    }

}