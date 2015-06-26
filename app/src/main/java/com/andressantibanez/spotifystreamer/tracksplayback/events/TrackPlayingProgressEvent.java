package com.andressantibanez.spotifystreamer.tracksplayback.events;

import kaaes.spotify.webapi.android.models.Track;

public class TrackPlayingProgressEvent {

    Track track;
    int position;

    public TrackPlayingProgressEvent(Track track, int position) {
        this.track = track;
        this.position = position;
    }

    public Track getTrack() {
        return track;
    }
    public int getPosition() {
        return position;
    }

    public static TrackPlayingProgressEvent newInstance(Track track, int position) {
        return new TrackPlayingProgressEvent(track, position);
    }
}
