package com.spadatech.spotifystreamer.fragments;


import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.models.MyTracks;
import com.spadatech.spotifystreamer.services.MediaPlayerService;
import com.spadatech.spotifystreamer.services.MediaPlayerService.MusicBinder;
import com.spadatech.spotifystreamer.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class MediaPlayerDialogFragment extends DialogFragment implements View.OnClickListener{
    public static final String TRACKS = "tracks_key";
    public static final String TRACK_NUMBER = "track_number_key";
    public static final String MEDIA_PLAYER_DIALOG = "media_player_dialog";

    // Layout variables
    TextView tvArtistName;
    TextView tvAlbumName;
    TextView tvSongName;
    TextView tvCurrentTime;
    TextView tvTotalTime;

    ImageView ivCoverPicture;

    ImageButton ibPrevious;
    ImageButton ibPlayPause;
    ImageButton ibNext;
    ImageButton ibShuffle;
    ImageButton ibRepeat;
    ImageButton ibShare;

    SeekBar seekbar;

    // Layout helper variables
    String currentTime = "0:00";
    Handler seekHandler = new Handler();
    Boolean repeat = false;
    Boolean shuffle = false;

    // Data Variables
    ArrayList<MyTracks> mTracks;
    int mCurrentTrack = 0;

    // MediaPlayer/Service variables
    MediaPlayerService mMediaPlayerService;
    Intent mediaPlayerIntent;
    boolean isMediaPlayerBound = false;
    boolean isOrientationChage = false;
    boolean mTwoPanel;

    public MediaPlayerDialogFragment() {
    }

    public static MediaPlayerDialogFragment newInstance(ArrayList<MyTracks> tracks, int currentTrack)
    {
        MediaPlayerDialogFragment fragment = new MediaPlayerDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(TRACKS, tracks);
        bundle.putInt(TRACK_NUMBER, currentTrack);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwoPanel = getResources().getBoolean(R.bool.dual_pane);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("nowPlaying"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_media_player, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        if(getArguments() != null) {
            mTracks = getArguments().getParcelableArrayList(TRACKS);
            mCurrentTrack = getArguments().getInt(TRACK_NUMBER);
        }

        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey("bound"))
                isMediaPlayerBound = savedInstanceState.getBoolean("bound");
            if(savedInstanceState.containsKey("shuffle"))
                shuffle = savedInstanceState.getBoolean("shuffle");
            if(savedInstanceState.containsKey("repeat"))
                repeat = savedInstanceState.getBoolean("repeat");
            if(savedInstanceState.containsKey("instance_current_tracks"))
                mCurrentTrack = savedInstanceState.getInt("instance_current_tracks");
            if(savedInstanceState.containsKey("instance_current_time"))
                currentTime = savedInstanceState.getString("instance_current_time");
            if(savedInstanceState.containsKey("instance_tracks"))
                mTracks = savedInstanceState.getParcelableArrayList("instance_tracks");
            isOrientationChage = true;
        }

        tvArtistName = (TextView) view.findViewById(R.id.tv_artist_name);
        tvAlbumName = (TextView) view.findViewById(R.id.tv_album_name);
        tvSongName = (TextView) view.findViewById(R.id.tv_song_name);
        tvCurrentTime = (TextView) view.findViewById(R.id.tv_current_time);
        tvTotalTime = (TextView) view.findViewById(R.id.tv_total_time);

        ivCoverPicture = (ImageView) view.findViewById(R.id.iv_cover_picture);

        ibPrevious = (ImageButton) view.findViewById(R.id.ib_previous);
        ibPrevious.setOnClickListener(this);
        ibPlayPause = (ImageButton) view.findViewById(R.id.ib_play_pause);
        ibPlayPause.setOnClickListener(this);
        ibNext = (ImageButton) view.findViewById(R.id.ib_next);
        ibNext.setOnClickListener(this);
        ibShuffle = (ImageButton) view.findViewById(R.id.ib_shuffle);
        ibShuffle.setOnClickListener(this);
        ibRepeat = (ImageButton) view.findViewById(R.id.ib_repeat);
        ibRepeat.setOnClickListener(this);
        ibShare = (ImageButton) view.findViewById(R.id.ib_share);
        ibShare.setOnClickListener(this);

        seekbar = (SeekBar) view.findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int position = seekBar.getProgress();
                mMediaPlayerService.seektTo(position);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
        if(mediaPlayerIntent == null){
            mediaPlayerIntent = new Intent(getActivity(), MediaPlayerService.class);
            mediaPlayerIntent.putExtra(Constants.TRACKS, mTracks);
            mediaPlayerIntent.putExtra(Constants.CURRENT_TRACK, mCurrentTrack);
            getActivity().startService(mediaPlayerIntent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //getActivity().unbindService(mediaPlayerConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().bindService(mediaPlayerIntent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
        if(isOrientationChage){
            isMediaPlayerBound = true;
            seekHandler.post(updateSeekBar);
            updateShuffleIcon(shuffle);
            updateRepeatIcon(repeat);
        }

        updateMediaPlayerViews();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mCurrentTrack = intent.getIntExtra(Constants.CURRENT_TRACK, 0);
            currentTime = intent.getStringExtra(Constants.CURRENT_TIME);
            mTracks = intent.getParcelableArrayListExtra(Constants.TRACKS);
            updateMediaPlayerViews();
        }
    };

    private ServiceConnection mediaPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            mMediaPlayerService = binder.getService();
            mMediaPlayerService.setPlayList(mTracks);
            mMediaPlayerService.setCurrentSong(mCurrentTrack);
            if(!isMediaPlayerBound){
                seekHandler.postDelayed(updateSeekBar, 100);
            }
            if (!mMediaPlayerService.isSameTrack())
            {
                mMediaPlayerService.playTrack();
            }
            isMediaPlayerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMediaPlayerBound = false;
            //mMediaPlayerService = null;
        }
    };

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.ib_play_pause:
                if (mMediaPlayerService.isPlaying()) {
                    mMediaPlayerService.pauseTrack();
                    seekHandler.removeCallbacks(updateSeekBar);
                    ibPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    mMediaPlayerService.resumeTrack();
                    seekHandler.postDelayed(updateSeekBar, 100);
                    ibPlayPause.setImageResource(R.drawable.ic_pause);
                }
                break;
            case R.id.ib_previous:
                mCurrentTrack = mMediaPlayerService.playPrevious();
                break;
            case R.id.ib_next:
                mCurrentTrack = mMediaPlayerService.playNext();
                break;
            case R.id.ib_repeat:
                repeat = mMediaPlayerService.turnOnRepeateat();
                updateRepeatIcon(repeat);
                break;
            case R.id.ib_shuffle:
                shuffle = mMediaPlayerService.turnOnShuffle();
                updateShuffleIcon(shuffle);
                break;
            case R.id.ib_share:
                shareSong();
                break;
        }
    }

    private void updateShuffleIcon(Boolean activate){
        if(activate){
            ibShuffle.setImageResource(R.drawable.ic_shuffle_active);
        }else{
            ibShuffle.setImageResource(R.drawable.ic_shuffle);
        }
    }

    private void updateRepeatIcon(Boolean activate){
        if(activate){
            ibRepeat.setImageResource(R.drawable.ic_repeat_active);
        }else{
            ibRepeat.setImageResource(R.drawable.ic_repeat);
        }
    }


    //handler to change seekBarTime
    private Runnable updateSeekBar = new Runnable() {
        public void run() {
            if(isMediaPlayerBound) {
                int totalDuration = getDuration();
                int currentProgression = getCurrentPosition();

                seekbar.setMax(totalDuration);
                seekbar.setProgress(currentProgression);

                String timeElapsed = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(currentProgression),
                        TimeUnit.MILLISECONDS.toSeconds(currentProgression) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentProgression))
                );

                String totalTimeFormatted = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                        TimeUnit.MILLISECONDS.toSeconds(totalDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration))
                );

                tvTotalTime.setText(totalTimeFormatted);
                tvCurrentTime.setText(timeElapsed);

                updateMediaPlayerViews();
            }

            //repeat yourself again in 100 miliseconds
            seekHandler.postDelayed(this, 100);
        }
    };

    public void shareSong() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mTracks.get(mCurrentTrack).getPreview_url());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void updateMediaPlayerViews(){
        tvArtistName.setText(mTracks.get(mCurrentTrack).getArtist());
        tvAlbumName.setText(mTracks.get(mCurrentTrack).getAlbum());
        tvSongName.setText(mTracks.get(mCurrentTrack).getName());
        Picasso.with(getActivity())
                .load(mTracks.get(mCurrentTrack).getCoverImgs())
                .placeholder(R.drawable.album_art_missing)
                .into(ivCoverPicture);

        if(mMediaPlayerService != null && mMediaPlayerService.isPlaying()) {
            ibPlayPause.setImageResource(R.drawable.ic_pause);
        }else if(mMediaPlayerService != null && !mMediaPlayerService.isPlaying()) {
            ibPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    public int getDuration() {
        if(mMediaPlayerService != null && isMediaPlayerBound && mMediaPlayerService.isPlaying()){
            return mMediaPlayerService.getDuration();
        }else{
            return 0;
        }
    }

    public int getCurrentPosition() {
        if(mMediaPlayerService != null && isMediaPlayerBound && mMediaPlayerService.isPlaying()){
            return mMediaPlayerService.getPosition();
        }else{
            return 0;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("bound", isMediaPlayerBound);
        outState.putBoolean("shuffle", shuffle);
        outState.putBoolean("repeat", repeat);
        outState.putInt("instance_current_tracks", mCurrentTrack);
        outState.putString("instance_current_time", currentTime);
        outState.putParcelableArrayList("instance_tracks", mTracks);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        seekHandler.removeCallbacks(updateSeekBar);
        super.onDestroy();
    }
}
