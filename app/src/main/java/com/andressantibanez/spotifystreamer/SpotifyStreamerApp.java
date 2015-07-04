package com.andressantibanez.spotifystreamer;

import android.app.Application;
import android.util.Log;

import kaaes.spotify.webapi.android.models.Track;

public class SpotifyStreamerApp extends Application {

    public static final String TAG = SpotifyStreamerApp.class.getSimpleName();

    private Track mCurrentTrack;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Track getCurrentTrack() {
        return mCurrentTrack;
    }

    public void setCurrentTrack(Track track) {
        mCurrentTrack = track;
        Log.d(TAG, "Current track set");
    }
}
