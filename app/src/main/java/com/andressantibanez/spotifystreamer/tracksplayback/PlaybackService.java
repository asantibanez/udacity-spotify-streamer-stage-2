package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.SpotifyStreamerApp;
import com.andressantibanez.spotifystreamer.Utils;
import com.andressantibanez.spotifystreamer.artistsearch.ArtistSearchActivity;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlaybackCompletedEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlayingProgressEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackToBePlayedEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

public class PlaybackService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String TAG = PlaybackService.class.getSimpleName();

    /**
     * Implementation notes
     * https://discussions.udacity.com/t/returning-to-the-song-currently-playing/21779/3
     * http://developer.android.com/guide/topics/media/mediaplayer.html#mpandservices
     */
    //Available Actions
    public static final String ACTION_PLAY_TRACK = "action_play_track";
    public static final String ACTION_PAUSE_TRACK = "action_pause_track";
    public static final String ACTION_RESUME_TRACK = "action_resume_track";
    public static final String ACTION_PLAY_PREVIOUS_TRACK = "action_previous_track";
    public static final String ACTION_PLAY_NEXT_TRACK = "action_next_track";
    public static final String ACTION_SET_TRACKS = "action_add_tracks";
    public static final String ACTION_SET_TRACK_PROGRESS_TO = "action_set_track_progress_to";
    public static final String ACTION_BROADCAST_CURRENT_TRACK = "action_broadcast_current_track";
    public static final int NOTIFICATION_ID = 3000;

    //Constants
    private static final String TRACKS_LIST = "tracks_list";
    private static final String TRACK_ID = "track_id";
    private static final String TRACK_PROGRESS = "track_progress";
    private static final String PREF_SHOW_PLAYBACK_CONTROLS_IN_LOCKSCREEN = "pref_show_playback_controls_in_lockscreen";

    //Variables
    Track mCurrentTrack;
    int mCurrentTrackIndex;
    MediaPlayer mMediaPlayer;
    BroadcastTrackProgressTask mBroadcastTrackProgressTask;
    List<Track> mTracksList;
    int mCurrentTrackThumbnailBitmap;

    /**
     * Constructor
     */
    public PlaybackService() {}

    /**
     * StartService Helpers
     */
    public static void setTracks(Context context, List<Track> tracksList) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_SET_TRACKS);
        serviceIntent.putExtra(TRACKS_LIST, new Gson().toJson(tracksList));
        context.startService(serviceIntent);
    }

    public static void playTrack(Context context, String trackId) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PLAY_TRACK);
        serviceIntent.putExtra(TRACK_ID, trackId);
        context.startService(serviceIntent);
    }

    public static void pauseTrack(Context context) {
        context.startService(getPauseTrackIntent(context));
    }

    public static Intent getPauseTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PAUSE_TRACK);
        return serviceIntent;
    }

    public static void resumeTrack(Context context) {
        context.startService(getResumeTrackIntent(context));
    }

    public static Intent getResumeTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_RESUME_TRACK);
        return serviceIntent;
    }

    public static void playNextTrack(Context context) {
        context.startService(getPlayNextTrackIntent(context));
    }

    public static Intent getPlayNextTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PLAY_NEXT_TRACK);
        return serviceIntent;
    }

    public static void playPreviousTrack(Context context) {
        context.startService(getPlayPreviousTrackIntent(context));
    }

    public static Intent getPlayPreviousTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PLAY_PREVIOUS_TRACK);
        return serviceIntent;
    }

    public static void setTrackProgressTo(Context context, int progress) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_SET_TRACK_PROGRESS_TO);
        serviceIntent.putExtra(TRACK_PROGRESS, progress);
        context.startService(serviceIntent);
    }

    public static void broadcastCurrentTrack(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_BROADCAST_CURRENT_TRACK);
        context.startService(serviceIntent);
    }

    /**
     * Binder interface
     */
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not available");
    }


    /**
     * Custom methods
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        //Cancel notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Set tracks
        if(intent.getAction().equals(ACTION_SET_TRACKS)) {
            setTracks(intent);
        }

        //Previous track
        if(intent.getAction().equals(ACTION_PLAY_PREVIOUS_TRACK)) {
            playPreviousTrack();
        }

        //Play track
        if(intent.getAction().equals(ACTION_PLAY_TRACK)) {
            String trackId = intent.getStringExtra(TRACK_ID);
            playTrack(trackId);
        }

        //Pause track
        if(intent.getAction().equals(ACTION_PAUSE_TRACK)) {
            pauseTrack();
        }

        //Resume track
        if(intent.getAction().equals(ACTION_RESUME_TRACK)) {
            resumeTrack();
        }

        //Next track
        if(intent.getAction().equals(ACTION_PLAY_NEXT_TRACK)) {
            playNextTrack();
        }

        //Set track progress
        if(intent.getAction().equals(ACTION_SET_TRACK_PROGRESS_TO)) {
            int progress = intent.getIntExtra(TRACK_PROGRESS, 0);
            setTrackProgressTo(progress);
        }

        //Request current track broadcast
        if(intent.getAction().equals(ACTION_BROADCAST_CURRENT_TRACK)) {
            if(mCurrentTrack != null)
                broadcastTrackToBePlayed();
        }

        return START_NOT_STICKY;
    }

    private Track getTrackById(String trackId) {
        for(Track track : mTracksList) {
            if(track.id.equals(trackId))
                return track;
        }

        //Should not happen
        return null;
    }

    private void setTracks(Intent data) {
        Type type = new TypeToken<List<Track>>() {}.getType();
        mTracksList = new Gson().fromJson(data.getStringExtra(TRACKS_LIST), type);
    }

    private void playPreviousTrack() {
        int previousTrackIndex = mCurrentTrackIndex - 1;
        if(mTracksList == null || previousTrackIndex < 0)
            return;

        Track track = mTracksList.get(previousTrackIndex);
        playTrack(track.id);
    }

    private void playNextTrack() {
        int nextTrackIndex = mCurrentTrackIndex + 1;

        if(mTracksList == null || nextTrackIndex >= mTracksList.size())
            return;

        Track track = mTracksList.get(nextTrackIndex);
        playTrack(track.id);
    }

    private void stopPlayback() {
        if(mMediaPlayer == null)
            return;

        if(mMediaPlayer.isPlaying())
            mMediaPlayer.stop();

        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        if(mBroadcastTrackProgressTask != null)
            mBroadcastTrackProgressTask.cancel(true);
    }

    private void playTrack(String trackId) {
        //Stop playback
        stopPlayback();

        //Get track
        mCurrentTrack = getTrackById(trackId);
        mCurrentTrackIndex = mTracksList.indexOf(mCurrentTrack);
        String trackUrl = mCurrentTrack.preview_url;

        //Notify track to be played
        broadcastTrackToBePlayed();

        //Set current track in app
        SpotifyStreamerApp app = (SpotifyStreamerApp) getApplication();
        app.setCurrentTrack(mCurrentTrack);

        //Start Media Player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        try {
            mMediaPlayer.setDataSource(trackUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseTrack() {
        if(mMediaPlayer == null)
            return;

        mMediaPlayer.pause();

        if(mBroadcastTrackProgressTask != null)
            mBroadcastTrackProgressTask.cancel(true);

        showNotification();
    }

    private void resumeTrack() {
        if(mMediaPlayer == null)
            return;

        mMediaPlayer.start();

        mBroadcastTrackProgressTask = new BroadcastTrackProgressTask();
        mBroadcastTrackProgressTask.execute();

        showNotification();
    }

    private void setTrackProgressTo(int progress) {
        if(mMediaPlayer == null)
            broadcastTrackPlaybackCompleted();

        if(mMediaPlayer.isPlaying())
           mMediaPlayer.seekTo(progress);
    }

    /**
     * Player broadcasts
     */
    private void broadcastTrackToBePlayed() {
        TrackToBePlayedEvent event = new TrackToBePlayedEvent(mCurrentTrack);
        EventBus.getDefault().post(event);

        showNotification();
    }

    private void broadcastTrackPlayingProgress() {
        TrackPlayingProgressEvent event = TrackPlayingProgressEvent.newInstance(
                mCurrentTrack,
                mMediaPlayer.getCurrentPosition(),
                mMediaPlayer.getDuration()
        );
        EventBus.getDefault().post(event);
    }

    private void broadcastTrackPlaybackCompleted() {
        TrackPlaybackCompletedEvent event = new TrackPlaybackCompletedEvent(mCurrentTrack);
        EventBus.getDefault().post(event);

        showNotification();
    }


    /**
     * Notifications
     */
    private void showNotificationUsingCustomLayout() {

        //New Remote View
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notification_playback);
        remoteView.setTextViewText(R.id.track_name, mCurrentTrack.name);
        remoteView.setTextViewText(R.id.artist_name, mCurrentTrack.artists.get(0).name);

        //Playback controls
        //Previous Track Intent
        remoteView.setOnClickPendingIntent(
                R.id.play_previous_track,
                PendingIntent.getService(this, 0, getPlayPreviousTrackIntent(this), 0)
        );

        //Resume/Pause
        remoteView.setViewVisibility(R.id.pause_track, View.VISIBLE);
        remoteView.setViewVisibility(R.id.resume_track, View.VISIBLE);
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            remoteView.setViewVisibility(R.id.resume_track, View.GONE);
            remoteView.setOnClickPendingIntent(
                    R.id.pause_track,
                    PendingIntent.getService(this, 0, getPauseTrackIntent(this), 0)
            );
        }
        else {
            remoteView.setViewVisibility(R.id.pause_track, View.GONE);
            remoteView.setOnClickPendingIntent(
                    R.id.resume_track,
                    PendingIntent.getService(this, 0, getResumeTrackIntent(this), 0)
            );
        }


        //Next Track Intent
        remoteView.setOnClickPendingIntent(
                R.id.play_next_track,
                PendingIntent.getService(this, 0, getPlayNextTrackIntent(this), 0)
        );

        //Content action
        //Show App Intent
        Intent showAppIntent = new Intent(this, ArtistSearchActivity.class);
        showAppIntent.setAction(Intent.ACTION_MAIN);
        showAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent showAppPendingIntent = PendingIntent.getActivity(this, 0, showAppIntent, 0);

        //Prepare notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContent(remoteView)
                .setContentIntent(showAppPendingIntent);

        //Check if ongoing notification
        notificationBuilder.setOngoing(mMediaPlayer != null && mMediaPlayer.isPlaying());

        //Show playback controls in lockscreen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showPlaybackControlsInLockScreen = sharedPreferences.getBoolean(PREF_SHOW_PLAYBACK_CONTROLS_IN_LOCKSCREEN, true);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH && showPlaybackControlsInLockScreen) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        //Display notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        //Thumbnail
        String thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 0);
        if(thumbnailUrl != null)
            Picasso.with(this).load(thumbnailUrl).into(remoteView, R.id.album_thumbnail, NOTIFICATION_ID, notification);
    }

    private void showNotificationUsingCompat() {

        //Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(mCurrentTrack.name);
        notificationBuilder.setContentText(mCurrentTrack.artists.get(0).name);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_media_play);

        //Show playback controls in lockscreen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showPlaybackControlsInLockScreen = sharedPreferences.getBoolean(PREF_SHOW_PLAYBACK_CONTROLS_IN_LOCKSCREEN, true);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH && showPlaybackControlsInLockScreen) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        //Playback controls
        //Previous track action
        notificationBuilder.addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                PendingIntent.getService(this, 0, getPlayPreviousTrackIntent(this), 0)
        );
        //Pause / Resume track action
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            notificationBuilder.addAction(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    PendingIntent.getService(this, 0, getPauseTrackIntent(this), 0)
            );
        } else {
            notificationBuilder.addAction(
                    android.R.drawable.ic_media_play,
                    "Resume",
                    PendingIntent.getService(this, 0, getResumeTrackIntent(this), 0)
            );
        }
        //Next track action
        notificationBuilder.addAction(
                android.R.drawable.ic_media_next,
                "Next",
                PendingIntent.getService(this, 0, getPlayNextTrackIntent(this), 0)
        );

        //Check if ongoing notification
        notificationBuilder.setOngoing(mMediaPlayer != null && mMediaPlayer.isPlaying());

        //Show App Intent
        Intent showAppIntent = new Intent(this, ArtistSearchActivity.class);
        showAppIntent.setAction(Intent.ACTION_MAIN);
        showAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, showAppIntent, 0));

        //Display notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        //Show thumbnail if available
        String thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 0);
    }

    private void showNotification() {
        Log.d(TAG, "Displaying notification");
        showNotificationUsingCustomLayout();
        //showNotificationUsingCompat();
    }


    /**
     * BroadcastTrackProgressTask: reports song that is being played and progress
     */
    class BroadcastTrackProgressTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            while(!isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(!mMediaPlayer.isPlaying())
                    return null;

                broadcastTrackPlayingProgress();
            }

            return null;
        }
    }


    /**
     * MediaPlayer implementation
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        broadcastTrackToBePlayed();
        resumeTrack();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(TAG, "Error during Playback!");
        Toast.makeText(this, R.string.playback_error, Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        broadcastTrackPlaybackCompleted();
    }
}
