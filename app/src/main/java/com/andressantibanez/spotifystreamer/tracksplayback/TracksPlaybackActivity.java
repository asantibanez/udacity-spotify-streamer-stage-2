package com.andressantibanez.spotifystreamer.tracksplayback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.andressantibanez.spotifystreamer.R;

public class TracksPlaybackActivity extends AppCompatActivity {

    public static void launch(Context context) {
        Intent activity = new Intent(context, TracksPlaybackActivity.class);
        context.startActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks_playback);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_tracks_playback, TracksPlaybackFragment.newInstance())
                .commit();
    }

}
