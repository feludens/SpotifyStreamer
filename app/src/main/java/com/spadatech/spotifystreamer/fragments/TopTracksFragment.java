package com.spadatech.spotifystreamer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.adapters.TracksArrayAdapter;
import com.spadatech.spotifystreamer.models.Album;
import com.spadatech.spotifystreamer.models.MyTracks;
import com.spadatech.spotifystreamer.utils.Constants;
import com.squareup.picasso.Picasso;

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

public class TopTracksFragment extends Fragment implements AbsListView.OnScrollListener{

    //SPOTIFY related variables
    private SpotifyApi spotifyApi = new SpotifyApi();
    private SpotifyService spotifyService = spotifyApi.getService();

    //Layout Elements
    private ListView tracksList;
    MenuItem playerItem;
    boolean visible = false;

    //Data variables
    private ArrayList<MyTracks> mTracks;
    private String artistId;
    private Album album;

    //Others
    private ProgressDialog ppDialog;
    private TracksArrayAdapter adapter;
    private boolean isOrientationChange = false;
    int currentTrack = 0;
    Map<String, Object> options = new HashMap<>();
    ViewGroup header;
    SharedPreferences sharedPreferences;
    ImageView mHeaderImage;
    private int lastTopValue = 0;

    public TopTracksFragment() {
        // Required empty public constructor
    }

    public static TopTracksFragment newInstance(Album newAlbum)
    {
        TopTracksFragment fragment = new TopTracksFragment();

        Bundle args = new Bundle();
        args.putParcelable(Constants.ALBUM, newAlbum);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        //if(savedInstanceState == null) {
        Bundle bundle = getArguments();
        if(bundle != null) {
            //artistId = bundle.getString("artistId");
            album = bundle.getParcelable(Constants.ALBUM);
        } else {
            //artistId = getActivity().getIntent().getStringExtra("artistId");
            album = getActivity().getIntent().getParcelableExtra(Constants.ALBUM);
        }

        if(album!=null){
            artistId = album.getArtistId();
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            mTracks = new ArrayList<MyTracks>();
            isOrientationChange = false;
        } else {
            mTracks = savedInstanceState.getParcelableArrayList("tracks");
            currentTrack = savedInstanceState.getInt("currentTrack");
            visible = savedInstanceState.getBoolean("playerItem");
            isOrientationChange = true;
        }

        adapter = new TracksArrayAdapter(getActivity(), mTracks);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String countryCode = sharedPreferences.getString(getActivity().
                        getString(R.string.pref_cc_key), (getActivity().getString(R.string.pref_cc_default)));

        options.put("country", countryCode);

        if (!isOrientationChange && artistId != null) {
            ppDialog = new ProgressDialog(getActivity());
            ppDialog.setMessage("Loading Tracks ....");
            populateList(artistId, options);
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("nowPlaying"));
        //}

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            currentTrack = intent.getIntExtra(Constants.CURRENT_TRACK, 0);
            mTracks = intent.getParcelableArrayListExtra(Constants.TRACKS);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        tracksList = (ListView) view.findViewById(R.id.lvTacks);
        tracksList.setAdapter(adapter);

        //Add heather to the list
        header = (ViewGroup) inflater.inflate(R.layout.header_top_tracks, tracksList, false);

        mHeaderImage = (ImageView) header.findViewById(R.id.iv_top_tracks_header);

        setupParallax(tracksList);

        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updatePosition(position-1);
                showMediaPlayerDialog();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void setupParallax(ListView tracksList){
        if(album != null) {
            Picasso.with(getActivity())
                    .load(album.getCoverImg())
                    .placeholder(R.drawable.album_art_missing)
                    .into(mHeaderImage);
        }

        tracksList.addHeaderView(header, null, false);
        tracksList.setOnScrollListener(this);
    }

    private void updatePosition(int pos){
        currentTrack = pos;
    }

    private void populateList(String artistId, Map<String, Object> options){
        spotifyService.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                List<Track> listOfTracks = tracks.tracks;

                if (listOfTracks.size() == 0) {
                    Toast.makeText(getActivity(), "Ops, no tracks found. Try again!", Toast.LENGTH_LONG).show();
                }

                for (Track item : listOfTracks) {
                    MyTracks track = new MyTracks();
                    track.setName(item.name);
                    track.setAlbum(item.album.name);
                    track.setArtist(item.artists.get(0).name);
                    track.setPreview_url(item.preview_url);
                    if (item.album.images.size() > 0) {
                        String url = item.album.images.get(0).url;
                        track.setCoverImgs(url);
                    }
                    mTracks.add(track);
                }
                adapter.notifyDataSetChanged();


                if (ppDialog.isShowing()) {
                    ppDialog.dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Ops, no tracks found. Try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", mTracks);
        outState.putInt("currentTrack", currentTrack);
        outState.putBoolean("playerItem", playerItem.isVisible());
        super.onSaveInstanceState(outState);
    }

    public void showMediaPlayerDialog(){
        visible = true;
        playerItem.setVisible(visible);

        MediaPlayerDialogFragment fragment =
                (MediaPlayerDialogFragment) getActivity().getSupportFragmentManager()
                        .findFragmentByTag(MediaPlayerDialogFragment.MEDIA_PLAYER_DIALOG);
        if (fragment != null) {
            fragment.show(getActivity().getSupportFragmentManager(), MediaPlayerDialogFragment.MEDIA_PLAYER_DIALOG);
        } else {
            MediaPlayerDialogFragment newFragment = MediaPlayerDialogFragment.newInstance(mTracks, currentTrack);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(newFragment, MediaPlayerDialogFragment.MEDIA_PLAYER_DIALOG);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_top_tracks_fragment, menu);
        playerItem = menu.findItem(R.id.action_open_player);
        playerItem.setVisible(visible);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_open_player){
            showMediaPlayerDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Rect rect = new Rect();
        mHeaderImage.getLocalVisibleRect(rect);
        if (lastTopValue != rect.top) {
            lastTopValue = rect.top;
            mHeaderImage.setY((float) (rect.top / 2.0));
        }
    }
}
