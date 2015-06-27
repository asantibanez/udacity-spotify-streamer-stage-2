package com.andressantibanez.spotifystreamer.tracksplayback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.andressantibanez.spotifystreamer.R;

public class TracksPlaybackActivity extends AppCompatActivity {

    public static final String TAG = TracksPlaybackActivity.class.getSimpleName();

    //Constants
    public static final String TRACK_TO_PLAY_ID = "track_to_play_id";

    /**
     * Launch helpers
     */
    public static void launch(Context context, String trackId) {
        Intent activity = new Intent(context, TracksPlaybackActivity.class);
        activity.putExtra(TRACK_TO_PLAY_ID, trackId);
        context.startActivity(activity);
    }

    /**
     * Lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks_playback);

        if(savedInstanceState == null) {

            String trackId = getIntent().getStringExtra(TRACK_TO_PLAY_ID);

            TracksPlaybackFragment fragment = TracksPlaybackFragment.newInstance(trackId);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_tracks_playback, fragment)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Menu methods
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
