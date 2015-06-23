package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String TAG = PlaybackService.class.getSimpleName();

    /**
     * Implementation notes
     * https://discussions.udacity.com/t/returning-to-the-song-currently-playing/21779/3
     * http://developer.android.com/guide/topics/media/mediaplayer.html#mpandservices
     */
    //Available Actions
    public static final String ACTION_PLAY = "play_song";
    public static final String ACTION_STOP = "stop_song";
    public static final String ACTION_PREVIOUS_SONG = "previous_song";
    public static final String ACTION_NEXT_SONG = "next_song";

    //Variables
    MediaPlayer mMediaPlayer;

    /**
     * Constructor
     */
    public PlaybackService() {
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

        String trackUrl = "https://p.scdn.co/mp3-preview/36b593fd8b5a0b1ccc671a1d7972bcca6a2ca063";

        Log.i(TAG, trackUrl);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(trackUrl);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
        } catch (IllegalArgumentException e1) {
            Log.e(TAG, "Error opening stream: " + e1);
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
            Log.e(TAG, "Error opening stream: " + e2);
        }

        return START_NOT_STICKY;
    }



    /**
     * MediaPlayer implementation
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "Starting playback");

        mMediaPlayer = mediaPlayer;
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(TAG, "Error thrown!");
        return false;
    }
}
