package com.andressantibanez.spotifystreamer.tracksplayback.events;

import kaaes.spotify.webapi.android.models.Track;

public class TrackToBePlayedEvent {

    Track mTrack;

    public TrackToBePlayedEvent(Track track) {
        mTrack = track;
    }

    public static TrackToBePlayedEvent newInstance(Track track) {
        return new TrackToBePlayedEvent(track);
    }

    public Track getTrack() {
        return mTrack;
    }

}
