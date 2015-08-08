package com.spadatech.spotifystreamer.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.spadatech.spotifystreamer.adapters.TracksArrayAdapter;
import com.spadatech.spotifystreamer.models.MyTracks;
import com.spadatech.spotifystreamer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracksActivity extends AppCompatActivity {

    //Layout Elements
    private Toolbar toolbar;
    private ListView tracksList;

    //SPOTIFY related variables
    private SpotifyApi spotifyApi = new SpotifyApi();
    private SpotifyService spotifyService = spotifyApi.getService();

    //Data variables
    private ArrayList<MyTracks> mTracks;
    private String artistId;
    private String artistName;

    //Others
    private ProgressDialog ppDialog;
    private TracksArrayAdapter adapter;
    private boolean isOrientationChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artistId = getIntent().getStringExtra("artistId");
        artistName = getIntent().getStringExtra("artistName");

        setContentView(R.layout.activity_top_tracks);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tracksList = (ListView) findViewById(R.id.lvTacks);

        if(savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            mTracks = new ArrayList<MyTracks>();
            isOrientationChange = false;
        }
        else {
            mTracks = savedInstanceState.getParcelableArrayList("tracks");
            isOrientationChange = true;
        }

        adapter = new TracksArrayAdapter(getApplicationContext(), mTracks);
        tracksList.setAdapter(adapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(artistName);
        toolbar.setSubtitle("TOP TRACKS");

        ppDialog = new ProgressDialog(this);
        ppDialog.setMessage("Loading Image ....");

        Map<String, Object> options = new HashMap<>();
        options.put("country", "US");

        if(!isOrientationChange) {
            populateList(artistId, options);
        }

    }

    private void populateList(String artistId, Map<String, Object> options){
        spotifyService.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                List<Track> listOfTracks = tracks.tracks;

                if(listOfTracks.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Ops, no tracks found. Try again!", Toast.LENGTH_LONG).show();
                }

                for (Track item : listOfTracks) {
                    MyTracks track = new MyTracks();
                    track.setName(item.name);
                    track.setAlbum(item.album.name);
                    if (item.album.images.size() > 0) {
                        String url = item.album.images.get(0).url;
                        track.setCoverImg(url);
                    }
                    mTracks.add(track);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Ops, no tracks found. Try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", mTracks);
        super.onSaveInstanceState(outState);
    }

}