package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackIsPlayingEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String TAG = PlaybackService.class.getSimpleName();

    /**
     * Implementation notes
     * https://discussions.udacity.com/t/returning-to-the-song-currently-playing/21779/3
     * http://developer.android.com/guide/topics/media/mediaplayer.html#mpandservices
     */
    //Available Actions
    public static final String ACTION_PLAY_TRACK = "play_song";
    public static final String ACTION_STOP = "stop_song";
    public static final String ACTION_PREVIOUS_SONG = "previous_song";
    public static final String ACTION_NEXT_SONG = "next_song";
    public static final String ACTION_ADD_TRACKS = "add_tracks";

    //Constants
    private static final String TRACKS_LIST = "tracks_list";
    private static final String TRACK_ID = "track_id";

    //Variables
    Track mCurrentTrack;
    MediaPlayer mMediaPlayer;
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
        serviceIntent.setAction(ACTION_ADD_TRACKS);
        serviceIntent.putExtra(TRACKS_LIST, new Gson().toJson(tracksList));
        context.startService(serviceIntent);
    }

    public static void playTrack(Context context, String trackId) {
        Intent serviceIntent = new Intent(context, PlaybackService.class);
        serviceIntent.setAction(ACTION_PLAY_TRACK);
        serviceIntent.putExtra(TRACK_ID, trackId);
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
        if(intent.getAction().equals(ACTION_ADD_TRACKS)) {
            setTracks(intent);
        }

        //Play track
        if(intent.getAction().equals(ACTION_PLAY_TRACK)) {
            String trackId = intent.getStringExtra(TRACK_ID);
            playTrack(trackId);
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
        mTracksList = new ArrayList<>();

        Type type = new TypeToken<List<Track>>() {}.getType();
        mTracksList = new Gson().fromJson(data.getStringExtra(TRACKS_LIST), type);

        Log.i(TAG, "Tracks set: " + mTracksList.size());
    }

    private void playTrack(String trackId) {

        try {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCurrentTrack = getTrackById(trackId);
        String trackUrl = mCurrentTrack.preview_url;

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(trackUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
        } catch (IllegalArgumentException e1) {
            Log.e(TAG, "Error opening stream: " + e1);
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
            Log.e(TAG, "Error opening stream: " + e2);
        }
    }

    private void showTrackPlayingNotification() {
        EventBus.getDefault().post(new TrackIsPlayingEvent(mCurrentTrack));
    }


    /**
     * MediaPlayer implementation
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "Starting playback");
        mMediaPlayer = mediaPlayer;
        mMediaPlayer.start();

        showTrackPlayingNotification();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(TAG, "Error thrown!");
        return false;
    }
}
