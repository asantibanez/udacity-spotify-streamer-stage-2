package com.andressantibanez.spotifystreamer.artisttopsongs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.common.BaseActivity;
import com.andressantibanez.spotifystreamer.tracksplayback.PlaybackService;
import com.andressantibanez.spotifystreamer.tracksplayback.TracksPlaybackActivity;
import com.andressantibanez.spotifystreamer.tracksplayback.TracksPlaybackFragment;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlaybackCompletedEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlayingProgressEvent;


import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;


public class ArtistTopTracksActivity extends BaseActivity implements
        ArtistTopTracksFragment.InteractionListener {

    public static final String TAG = ArtistTopTracksActivity.class.getSimpleName();

    //Constants
    private static final String ARTIST_ID = "artist_id";
    private static final String ARTIST_NAME = "artist_name";

    //Variables
    String mArtistId;
    String mArtistName;

    /**
     * Intent factory
     */
    public static Intent launchIntent(Context context, String artistId, String artistName) {
        Intent launchIntent = new Intent(context, ArtistTopTracksActivity.class);
        launchIntent.putExtra(ARTIST_ID, artistId);
        launchIntent.putExtra(ARTIST_NAME, artistName);
        return launchIntent;
    }

    /**
     * Lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_top_tracks);
        ButterKnife.inject(this);

        mArtistId = getIntent().getStringExtra(ARTIST_ID);
        mArtistName = getIntent().getStringExtra(ARTIST_NAME);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_ten_tracks);
        actionBar.setSubtitle(mArtistName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Setup fragment
        FragmentManager fm = getSupportFragmentManager();
        ArtistTopTracksFragment fragment = (ArtistTopTracksFragment) fm.findFragmentById(R.id.fragment_artist_top_tracks);
        if(fragment == null) {
            fm.beginTransaction()
                    .replace(R.id.fragment_artist_top_tracks, ArtistTopTracksFragment.newInstance(mArtistId, mArtistName))
                    .commit();
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Menu methods
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        if(id == R.id.action_now_playing) {
            TracksPlaybackActivity.launch(this, null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * ArtistTopTracksFragment.InteractionListener implementation
     */
    @Override
    public void onTrackSelected(List<Track> tracksList, String trackId) {
        PlaybackService.setTracks(this, tracksList);
        TracksPlaybackActivity.launch(this, trackId);
    }


    /**
     * BaseActivity implementation
     */
    @Override
    public boolean displayPreferencesMenuItem() {
        return false;
    }
}
