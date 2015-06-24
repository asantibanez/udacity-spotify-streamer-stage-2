package com.andressantibanez.spotifystreamer.tracksplayback.events;

import kaaes.spotify.webapi.android.models.Track;

public class TrackIsPlayingEvent {

    Track track;

    public TrackIsPlayingEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }
}
