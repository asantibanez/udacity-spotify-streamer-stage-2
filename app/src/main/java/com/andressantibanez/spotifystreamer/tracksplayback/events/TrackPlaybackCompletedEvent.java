package com.andressantibanez.spotifystreamer.tracksplayback.events;

import kaaes.spotify.webapi.android.models.Track;

public class TrackPlaybackCompletedEvent {

    Track mTrack;

    public TrackPlaybackCompletedEvent(Track track) {
        mTrack = track;
    }

    public static TrackPlaybackCompletedEvent newInstance(Track track) {
        return new TrackPlaybackCompletedEvent(track);
    }

    public Track getTrack() {
        return mTrack;
    }

}
