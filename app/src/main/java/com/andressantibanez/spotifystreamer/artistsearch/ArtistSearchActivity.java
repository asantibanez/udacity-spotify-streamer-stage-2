package com.andressantibanez.spotifystreamer.artistsearch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.artisttopsongs.ArtistTopTracksActivity;

import butterknife.ButterKnife;

public class ArtistSearchActivity extends AppCompatActivity implements ArtistSearchFragment.InteractionListener {

    static final String TAG = ArtistSearchActivity.class.getSimpleName();

    /**
     * Lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);
        ButterKnife.inject(this);
    }

    /**
     * ArtistSearchFragment.InteractionListener implementation
     */
    @Override
    public void onArtistSelected(String artistId, String artistName) {
        startActivity(ArtistTopTracksActivity.launchIntent(this, artistId, artistName));
    }
}
