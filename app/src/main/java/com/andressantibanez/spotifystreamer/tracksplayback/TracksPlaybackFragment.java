package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackIsPlayingEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

public class TracksPlaybackFragment extends DialogFragment {

    public static final String TAG = TracksPlaybackFragment.class.getSimpleName();

    //Variables

    //Controls
    @InjectView(R.id.artist_name) TextView mArtistName;
    @InjectView(R.id.album_name) TextView mAlbumName;
    @InjectView(R.id.track_name) TextView mTrackName;

    /**
     * Factory method
     */
    public static TracksPlaybackFragment newInstance() {
        TracksPlaybackFragment fragment = new TracksPlaybackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TracksPlaybackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks_playback, container, false);
        ButterKnife.inject(this, view);

        EventBus.getDefault().register(this);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);

        EventBus.getDefault().unregister(this);
    }

    public void onEvent(TrackIsPlayingEvent event) {
        Track track = event.getTrack();

        mArtistName.setText(track.artists.get(0).name);
        mAlbumName.setText(track.album.name);
        mTrackName.setText(track.name);
    }
}
