package com.andressantibanez.spotifystreamer.tracksplayback;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
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
    boolean mTrackProgressAutoUpdate;
    int mTrackProgressByUser;
    int mTrackProgress;
    int mTrackLength;
    boolean mIsPaused;

    //Controls
    @InjectView(R.id.artist_name) TextView mArtistNameTextView;
    @InjectView(R.id.album_name) TextView mAlbumNameTextView;
    @InjectView(R.id.track_name) TextView mTrackNameTextView;
    @InjectView(R.id.track_progress) SeekBar mTrackProgressSeekBar;
    @InjectView(R.id.play_previous_track) ImageButton mPlayPreviousTrackButton;
    @InjectView(R.id.play_track) ImageButton mPlayTrackButton;
    @InjectView(R.id.play_next_track) ImageButton mPlayNextTrackButton;
    @InjectView(R.id.album_thumbnail) ImageView mThumbnailImageView;
    @InjectView(R.id.track_position) TextView mTrackProgressTextView;
    @InjectView(R.id.track_length) TextView mTrackLengthTextView;

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
        mIsPaused = true;

        //Setup listeners
        mPlayTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsPaused)
                    PlaybackService.pauseTrack(getActivity());
                else
                    PlaybackService.resumeTrack(getActivity());

                mIsPaused = !mIsPaused;
                updatePlayTrackButton();
            }
        });
        mPlayPreviousTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackService.playPreviousTrack(getActivity());
            }
        });
        mPlayNextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackService.playNextTrack(getActivity());
            }
        });
        mTrackProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
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
        } else {
            //Request current track broadcast
            PlaybackService.broadcastCurrentTrack(getActivity());
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
    public void updatePlayTrackButton() {
        if(mIsPaused)
            mPlayTrackButton.setImageResource(android.R.drawable.ic_media_play);
        else
            mPlayTrackButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    public void updateCurrentTrack(Track track) {
        if(mCurrentTrack == null || !mCurrentTrack.id.equals(track.id)) {
            mCurrentTrack = track;
            displayTrackInfo();
        }
    }

    public void onEventMainThread(TrackToBePlayedEvent event) {
        updateCurrentTrack(event.getTrack());

        mIsPaused = true;
        updatePlayTrackButton();
    }

    public void onEventMainThread(TrackPlayingProgressEvent event) {
        updateCurrentTrack(event.getTrack());
        setTrackProgress(event.getProgress());
        setTrackMaxProgress(event.getMaxProgress());

        mIsPaused = false;
        updatePlayTrackButton();
    }

    public void onEventMainThread(TrackPlaybackCompletedEvent event) {
        updateCurrentTrack(event.getTrack());

        mIsPaused = true;
        updatePlayTrackButton();

        setTrackProgress(mTrackLength);
    }

    private void displayTrackInfo() {
        mTrackProgress = 0;
        mTrackLength = 0;


        mArtistNameTextView.setText(mCurrentTrack.artists.get(0).name);
        mAlbumNameTextView.setText(mCurrentTrack.album.name);
        mTrackNameTextView.setText(mCurrentTrack.name);
        mTrackProgressSeekBar.setProgress(0);
        mTrackProgressTextView.setText(Utils.millisecondsToMMSS(mTrackProgress));
        mTrackLengthTextView.setText(" - ");

        String thumbnailUrl;

        //Get thumbnail for album. 600 -> 300 -> 0
        thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 600);
        if(thumbnailUrl == null)
            thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 300);
        if(thumbnailUrl == null)
            thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 0);


        mThumbnailImageView.setImageBitmap(null);
        if(thumbnailUrl != null)
            Picasso.with(getActivity()).load(thumbnailUrl).into(mThumbnailImageView);
    }

    private void setTrackProgress(int progress) {
        if(!mTrackProgressAutoUpdate)
            return;

        mTrackProgress = progress;

        mTrackProgressSeekBar.setProgress(progress);
        mTrackProgressTextView.setText(Utils.millisecondsToMMSS(progress));
    }

    private void setTrackMaxProgress(int maxProgress) {
        if(mTrackLength == maxProgress)
            return;

        mTrackLength = maxProgress;

        mTrackProgressSeekBar.setMax(maxProgress);
        mTrackLengthTextView.setText(Utils.millisecondsToMMSS(maxProgress));
    }

}
