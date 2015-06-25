package com.andressantibanez.spotifystreamer.tracksplayback.events;

import kaaes.spotify.webapi.android.models.Track;

public class TrackProgressEvent {

    Track track;
    int position;

    public TrackProgressEvent(Track track, int position) {
        this.track = track;
        this.position = position;
    }

    public Track getTrack() {
        return track;
    }
    public int getPosition() {
        return position;
    }

    public static TrackProgressEvent newInstance(Track track, int position) {
        return new TrackProgressEvent(track, position);
    }
}
