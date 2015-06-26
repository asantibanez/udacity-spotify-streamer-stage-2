package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.Utils;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlayingProgressEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackToBePlayedEvent;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

public class TracksPlaybackFragment extends DialogFragment {

    public static final String TAG = TracksPlaybackFragment.class.getSimpleName();

    //Variables
    Track mCurrentTrack;

    //Controls
    @InjectView(R.id.artist_name) TextView mArtistName;
    @InjectView(R.id.album_name) TextView mAlbumName;
    @InjectView(R.id.track_name) TextView mTrackName;
    @InjectView(R.id.play_previous_track) Button mPlayPreviousTrack;
    @InjectView(R.id.play_next_track) Button mPlayNextTrack;
    @InjectView(R.id.album_thumbnail) ImageView mThumbnail;

    /**
     * Factory method
     */
    public static TracksPlaybackFragment newInstance() {
        TracksPlaybackFragment fragment = new TracksPlaybackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required constructor
     */
    public TracksPlaybackFragment() {}

    /**
     * Lifecycle methods
     */
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

        //Setup listeners
        mPlayPreviousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackService.playPreviousTrack(getActivity());
            }
        });
        mPlayNextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackService.playNextTrack(getActivity());
            }
        });

        //Register for Bus updates
        EventBus.getDefault().register(this);

        return view;
    }

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

        //Unregiter for Bus
        EventBus.getDefault().unregister(this);
    }

    /**
     * Events handling
     */
    public void updateCurrentTrack(Track track) {
        if(mCurrentTrack == null || !mCurrentTrack.id.equals(track.id)) {
            mCurrentTrack = track;
            displayTrackInfo();
        }
    }

    public void onEventMainThread(TrackToBePlayedEvent event) {
        updateCurrentTrack(event.getTrack());
    }

    public void onEventMainThread(TrackPlayingProgressEvent event) {
        updateCurrentTrack(event.getTrack());
    }

    private void displayTrackInfo() {
        mArtistName.setText(mCurrentTrack.artists.get(0).name);
        mAlbumName.setText(mCurrentTrack.album.name);
        mTrackName.setText(mCurrentTrack.name);

        String thumbnailUrl;

        //Get thumbnail for album. 600 -> 300 -> 0
        thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 600);
        if(thumbnailUrl == null)
            thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 300);
        if(thumbnailUrl == null)
            thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 0);


        mThumbnail.setImageBitmap(null);
        if(thumbnailUrl != null)
            Picasso.with(getActivity()).load(thumbnailUrl).into(mThumbnail);

    }

}
