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
import android.widget.SeekBar;
import android.widget.TextView;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.Utils;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlaybackCompletedEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlayingProgressEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackToBePlayedEvent;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

public class TracksPlaybackFragment extends DialogFragment {

    public static final String TAG = TracksPlaybackFragment.class.getSimpleName();

    //Constants
    public static final String TRACK_TO_PLAY_ID = "track_to_play_id";

    //Variables
    Track mCurrentTrack;
    int mMaxProgress;
    boolean mTrackProgressAutoUpdate;
    int mTrackProgressByUser;

    //Controls
    @InjectView(R.id.artist_name) TextView mArtistName;
    @InjectView(R.id.album_name) TextView mAlbumName;
    @InjectView(R.id.track_name) TextView mTrackName;
    @InjectView(R.id.track_progress) SeekBar mTrackProgress;
    @InjectView(R.id.play_previous_track) Button mPlayPreviousTrack;
    @InjectView(R.id.play_next_track) Button mPlayNextTrack;
    @InjectView(R.id.album_thumbnail) ImageView mThumbnail;

    /**
     * Factory method
     */
    public static TracksPlaybackFragment newInstance(String trackId) {
        TracksPlaybackFragment fragment = new TracksPlaybackFragment();
        Bundle args = new Bundle();
        args.putString(TRACK_TO_PLAY_ID, trackId);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks_playback, container, false);
        ButterKnife.inject(this, view);

        mTrackProgressAutoUpdate = true;

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
        mTrackProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    mTrackProgressByUser = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mTrackProgressAutoUpdate = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlaybackService.setTrackProgressTo(getActivity(), mTrackProgressByUser);
                mTrackProgressByUser = 0;
                mTrackProgressAutoUpdate = true;
            }
        });

        //Register for Bus updates
        EventBus.getDefault().register(this);

        //Play track if set in arguments. Only on start up
        if(savedInstanceState == null) {
            String trackToPlayId = getArguments().getString(TRACK_TO_PLAY_ID, null);
            if(trackToPlayId != null)
                PlaybackService.playTrack(getActivity(), trackToPlayId);
        }

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
        setTrackProgress(event.getProgress());
        setTrackMaxProgress(event.getMaxProgress());
    }

    public void onEventMainThread(TrackPlaybackCompletedEvent event) {
        updateCurrentTrack(event.getTrack());
        mTrackProgress.setProgress(0);
    }

    private void displayTrackInfo() {
        mArtistName.setText(mCurrentTrack.artists.get(0).name);
        mAlbumName.setText(mCurrentTrack.album.name);
        mTrackName.setText(mCurrentTrack.name);
        mTrackProgress.setProgress(0);
        mMaxProgress = 0;

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

    private void setTrackProgress(int progress) {
        if(mTrackProgressAutoUpdate)
            mTrackProgress.setProgress(progress);
    }

    private void setTrackMaxProgress(int maxProgress) {
        if(mMaxProgress == maxProgress)
            return;

        mMaxProgress = maxProgress;
        mTrackProgress.setMax(maxProgress);

    }

}
