package com.andressantibanez.spotifystreamer.artisttopsongs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.tracksplayback.PlaybackService;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Track;


public class ArtistTopTracksActivity extends AppCompatActivity
        implements ArtistTopTracksFragment.InteractionListener {

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

        return super.onOptionsItemSelected(item);
    }

    /**
     * ArtistTopTracksFragment.InteractionListener implementation
     */
    @Override
    public void onTrackSelected(List<Track> tracksList, String trackId) {
        PlaybackService.setTracks(this, tracksList);
        PlaybackService.playTrack(this, trackId);
    }
}
