package com.andressantibanez.spotifystreamer.artistsearch;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.artisttopsongs.ArtistTopTracksActivity;
import com.andressantibanez.spotifystreamer.artisttopsongs.ArtistTopTracksFragment;
import com.andressantibanez.spotifystreamer.tracksplayback.PlaybackService;
import com.andressantibanez.spotifystreamer.tracksplayback.TracksPlaybackFragment;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import kaaes.spotify.webapi.android.models.Track;

public class ArtistSearchActivity extends AppCompatActivity
        implements ArtistSearchFragment.InteractionListener,
        ArtistTopTracksFragment.InteractionListener {

    static final String TAG = ArtistSearchActivity.class.getSimpleName();

    //Variables
    boolean mTwoPane;

    //Controls
    @Optional @InjectView(R.id.fragment_artist_top_tracks) FrameLayout container;

    /**
     * Lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);
        ButterKnife.inject(this);

        //Check if two panes available
        mTwoPane = false;
        if(container != null) {
            mTwoPane = true;
        }
    }

    /**
     * ArtistSearchFragment.InteractionListener implementation
     */
    @Override
    public void onArtistSelected(String artistId, String artistName) {
        //Two pane adds fragment
        if(mTwoPane) {
            ArtistTopTracksFragment fragment = ArtistTopTracksFragment.newInstance(artistId, artistName);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(container.getId(), fragment).commit();
            return;
        }

        //One pane launches Activity
        startActivity(ArtistTopTracksActivity.launchIntent(this, artistId, artistName));
    }

    /**
     * ArtistTopTracksFragment.InteractionListener implementation
     */
    @Override
    public void onTrackSelected(List<Track> tracksList, String trackId) {
        PlaybackService.setTracks(this, tracksList);

        TracksPlaybackFragment fragment = TracksPlaybackFragment.newInstance(trackId);
        fragment.show(getSupportFragmentManager(), "dialog");
    }
}
