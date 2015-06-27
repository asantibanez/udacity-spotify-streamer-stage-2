package com.andressantibanez.spotifystreamer.tracksplayback.events;

import kaaes.spotify.webapi.android.models.Track;

public class TrackPlayingProgressEvent {

    Track mTrack;
    int mProgress;
    int mMaxProgress;

    public TrackPlayingProgressEvent(Track track, int progress, int maxProgress) {
        mTrack = track;
        mProgress = progress;
        mMaxProgress = maxProgress;
    }

    public Track getTrack() {
        return mTrack;
    }
    public int getProgress() {
        return mProgress;
    }
    public int getMaxProgress() {
        return mMaxProgress;
    }

    public static TrackPlayingProgressEvent newInstance(Track track, int progress, int maxProgress) {
        return new TrackPlayingProgressEvent(track, progress, maxProgress);
    }
}
