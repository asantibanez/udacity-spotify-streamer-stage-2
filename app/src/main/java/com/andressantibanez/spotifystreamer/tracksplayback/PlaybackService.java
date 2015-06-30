package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.artistsearch.ArtistSearchActivity;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlaybackCompletedEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlayingProgressEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackToBePlayedEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    public static final String ACTION_PLAY_TRACK = "action_play_song";
    public static final String ACTION_PLAY_PREVIOUS_TRACK = "action_previous_song";
    public static final String ACTION_PLAY_NEXT_TRACK = "action_next_track";
    public static final String ACTION_SET_TRACKS = "action_add_tracks";
    public static final String ACTION_SET_TRACK_PROGRESS_TO = "action_set_track_progress_to";

    //Constants
    private static final String TRACKS_LIST = "tracks_list";
    private static final String TRACK_ID = "track_id";
    private static final String TRACK_PROGRESS = "track_progress";

    //Variables
    Track mCurrentTrack;
    int mCurrentTrackIndex;
    MediaPlayer mMediaPlayer;
    BroadcastTrackProgressTask mBroadcastTrackProgressTask;
    List<Track> mTracksList;

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

    public static void playNextTrack(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PLAY_NEXT_TRACK);
        context.startService(serviceIntent);
    }

    public static void playPreviousTrack(Context context) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PLAY_PREVIOUS_TRACK);
        context.startService(serviceIntent);
    }

    public static void setTrackProgressTo(Context context, int progress) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_SET_TRACK_PROGRESS_TO);
        serviceIntent.putExtra(TRACK_PROGRESS, progress);
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

        //Next track
        if(intent.getAction().equals(ACTION_PLAY_NEXT_TRACK)) {
            playNextTrack();
        }

        //Set track progress
        if(intent.getAction().equals(ACTION_SET_TRACK_PROGRESS_TO)) {
            int progress = intent.getIntExtra(TRACK_PROGRESS, 0);
            setTrackProgressTo(progress);
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
        if(previousTrackIndex < 0)
            return;

        Track track = mTracksList.get(previousTrackIndex);
        playTrack(track.id);
    }

    private void playNextTrack() {
        int nextTrackIndex = mCurrentTrackIndex + 1;

        if(nextTrackIndex >= mTracksList.size())
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

        showNotification(null);
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
    }


    /**
     * Notifications
     */
    private void showNotification(Bitmap thumbnailBitmap) {
        //New Remote View
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notification_playback);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            remoteView.setTextColor(R.id.track_name, Color.BLACK);
            remoteView.setTextColor(R.id.artist_name, Color.BLACK);
        }
        remoteView.setTextViewText(R.id.track_name, mCurrentTrack.name);
        remoteView.setTextViewText(R.id.artist_name, mCurrentTrack.artists.get(0).name);

        //Prepare notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContent(remoteView);

        //Display notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1000, notificationBuilder.build());

        /*
        PendingIntent pendingIntent =

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ResultActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
        */
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

        mMediaPlayer.start();

        mBroadcastTrackProgressTask = new BroadcastTrackProgressTask();
        mBroadcastTrackProgressTask.execute();
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
