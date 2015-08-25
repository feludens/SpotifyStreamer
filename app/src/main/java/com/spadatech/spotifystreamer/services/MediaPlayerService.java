package com.spadatech.spotifystreamer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.spadatech.spotifystreamer.R;
import com.spadatech.spotifystreamer.activities.AlbumsListActivity;
import com.spadatech.spotifystreamer.activities.TopTracksActivity;
import com.spadatech.spotifystreamer.fragments.MediaPlayerDialogFragment;
import com.spadatech.spotifystreamer.models.MyTracks;
import com.spadatech.spotifystreamer.utils.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String ARGS_BUNDLE = "args_bundle_key";
    private final IBinder mMusicBinder = new MusicBinder();

    boolean sameTrack = false;
    MyTracks track;

    SharedPreferences sharedPreferences;
    String notificationsKey;

    boolean mTwoPane;

    private MediaPlayer mMediaPlayer;
    private ArrayList<MyTracks> mMyTracks;
    private int mCurrentTrack;

    private boolean repeat = false;
    private boolean shuffle = false;
    private Random random;

    //Notification Controll Intents
    Intent playIntent;
    Intent pauseIntent;
    Intent nextIntent;
    Intent previousIntent;
    PendingIntent playPendingIntent;
    PendingIntent pausePendingIntent;
    PendingIntent nextPendingIntent;
    PendingIntent previousPendingIntent;

    public MediaPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCurrentTrack = -1;
        mMediaPlayer = new MediaPlayer();
        random = new Random();
        createNotificationControlIntents();
        mTwoPane = getResources().getBoolean(R.bool.dual_pane);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        notificationsKey = getApplicationContext().getString(R.string.pref_notifications_key);
        startMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action!= null) {
                switch (action){
                    case Constants.PLAY:
                        resumeTrack();
                        break;
                    case Constants.PAUSE:
                        pauseTrack();
                        break;
                    case Constants.NEXT:
                        playNext();
                        break;
                    case Constants.PREVIOUS:
                        playPrevious();
                        break;
                }
            } else{
                if(intent.hasExtra(Constants.TRACKS) && intent.hasExtra(Constants.CURRENT_TRACK)) {

                    int tempCurrentTrack = intent.getIntExtra(Constants.CURRENT_TRACK, 0);
                    ArrayList<MyTracks> tempMyTracks = intent.getParcelableArrayListExtra(Constants.TRACKS);

                    boolean same = checkIfSameTrack(tempMyTracks, tempCurrentTrack);
                    setSameTrack(same);

                    mMyTracks = tempMyTracks;
                    mCurrentTrack = tempCurrentTrack;

                    if (mMyTracks != null && mMyTracks.get(mCurrentTrack) != null) {
                        track = mMyTracks.get(mCurrentTrack);
                    }
                    //playTrack();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public boolean isSameTrack(){
        return sameTrack;
    }

    public void setSameTrack(boolean isSameTrack){
        sameTrack = isSameTrack;
    }

    public boolean checkIfSameTrack(ArrayList<MyTracks> myNewTracks, int newCurrentTrack)
    {
        boolean same = false;
        // if track number is the same it may be the same track
        if (mCurrentTrack == newCurrentTrack)
        {
            if (myNewTracks != null && myNewTracks.size() >= mCurrentTrack)
            {
                MyTracks newTrack = myNewTracks.get(mCurrentTrack);

                // if the preview url is the same then it is the same track
                if (newTrack.getPreview_url().equals(track.getPreview_url()))
                {
                    same = true;
                }
            }
        }
        return same;
    }

    public void startMusicPlayer(){
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setPlayList(ArrayList<MyTracks> tracks){
        mMyTracks = tracks;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if(repeat){
            playTrack();
        }else{
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Ops. This track can't be played. Let's try the next one!", Toast.LENGTH_LONG).show();
        mMediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        sendTrackBroadcast();

        prepareNotification();
    }

    private void prepareNotification(){
        if(sharedPreferences.getBoolean(notificationsKey,
                Boolean.parseBoolean(getApplicationContext().
                        getString(R.string.pref_notifications_default)))) {
            createNotification();
        }
    }

    private void createNotification(){
        Target mTarget;
        String url = mMyTracks.get(mCurrentTrack).getCoverImgs();

        Intent notificationIntent;
        if(mTwoPane) {
            notificationIntent = new Intent(getApplicationContext(), AlbumsListActivity.class);
        }else{
            notificationIntent = new Intent(getApplicationContext(), TopTracksActivity.class);
        }
        notificationIntent.putParcelableArrayListExtra(MediaPlayerDialogFragment.TRACKS, mMyTracks);
        notificationIntent.putExtra(MediaPlayerDialogFragment.TRACK_NUMBER, mCurrentTrack);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(TopTracksActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notification);
        remoteView.setTextViewText(R.id.tv_song_name, track.getName());
        remoteView.setTextViewText(R.id.tv_artist_name, track.getArtist());

        remoteView.setOnClickPendingIntent(R.id.btn_play, playPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.btn_pause, pausePendingIntent);
        remoteView.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.btn_previous, previousPendingIntent);

        if(isPlaying()){
            remoteView.setViewVisibility(R.id.btn_pause, View.VISIBLE);
            remoteView.setViewVisibility(R.id.btn_play, View.GONE);
        }else {
            remoteView.setViewVisibility(R.id.btn_pause, View.GONE);
            remoteView.setViewVisibility(R.id.btn_play, View.VISIBLE);
        }

        // Create base notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(remoteView)
                        .setContentIntent(notificationPendingIntent);

        //BigPicture notification style
        final NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(track.getName());
        bigPictureStyle.setSummaryText(track.getArtist());

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                bigPictureStyle.bigPicture(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        Picasso.with(getApplicationContext())
                .load(url)
                .into(mTarget);

        builder.addAction(android.R.drawable.ic_media_previous, "", previousPendingIntent);
        if(isPlaying()) {
            builder.addAction(android.R.drawable.ic_media_pause, "", pausePendingIntent);
        }else{
            builder.addAction(android.R.drawable.ic_media_play, "", playPendingIntent);
        }
        builder.addAction(android.R.drawable.ic_media_next, "", nextPendingIntent);

        // Build the notification
        builder.setStyle(bigPictureStyle);
        builder.setTicker(track.getName());
        Notification notification = builder.build();

        // Get current track's image
        Picasso.with(getApplicationContext())
                .load(url)
                .into(remoteView,
                        R.id.iv_cover_picture,
                        Constants.NOTIFICATION_ID,
                        notification);

        startForeground(Constants.NOTIFICATION_ID, notification);
    }

    private void createNotificationControlIntents(){
        previousIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        previousIntent.setAction(Constants.PREVIOUS);
        previousPendingIntent = PendingIntent.getService(getApplicationContext(), 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        playIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        playIntent.setAction(Constants.PLAY);
        playPendingIntent = PendingIntent.getService(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        pauseIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        pauseIntent.setAction(Constants.PAUSE);
        pausePendingIntent = PendingIntent.getService(getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        nextIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        nextIntent.setAction(Constants.NEXT);
        nextPendingIntent = PendingIntent.getService(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public class MusicBinder extends Binder{
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

    public void setCurrentSong(int selectedSong){
        mCurrentTrack = selectedSong;
    }

    private void sendTrackBroadcast() {
        Intent intent = new Intent("nowPlaying");
        intent.putExtra(Constants.CURRENT_TRACK, mCurrentTrack);
        intent.putExtra(Constants.CURRENT_TIME, getPosition());
        intent.putExtra(Constants.TRACKS, mMyTracks);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //The methods below are used by the MediaPlayerDialogFragment
    //To allow controlling the playback from the user interface

    public void playTrack(){
        mMediaPlayer.reset();
        String trackUrl = mMyTracks.get(mCurrentTrack).getPreview_url();
        setSameTrack(false);
        track = mMyTracks.get(mCurrentTrack);

        try {
            mMediaPlayer.setDataSource(trackUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();
    }

    public void resumeTrack(){
        mMediaPlayer.start();
        prepareNotification();
    }

    public void pauseTrack(){
        mMediaPlayer.pause();
        prepareNotification();
    }

    public int playPrevious(){
        if(mCurrentTrack > 0){
            mCurrentTrack--;
        }else{
            mCurrentTrack = mMyTracks.size() -1;
        }
        playTrack();
        return mCurrentTrack;
    }

    public int playNext() {
        if (shuffle) {
            int shuffledTrack = mCurrentTrack;
            while (shuffledTrack == mCurrentTrack) {
                shuffledTrack = random.nextInt(mMyTracks.size());
            }
            mCurrentTrack = shuffledTrack;
        } else {
            mCurrentTrack++;
            if (mCurrentTrack == mMyTracks.size()) {
                mCurrentTrack = 0;
            }
        }
        playTrack();
        return mCurrentTrack;
    }

    public void seektTo(int position){
        mMediaPlayer.seekTo(position);
    }

    public Boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public int getPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    public Boolean turnOnRepeateat(){
        repeat = !repeat;
        return repeat;
    }

    public Boolean turnOnShuffle(){
        shuffle = !shuffle;
        return shuffle;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
        stopForeground(true);
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
