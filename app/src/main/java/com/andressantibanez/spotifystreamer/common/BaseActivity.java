package com.andressantibanez.spotifystreamer.common;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.SpotifyStreamerApp;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlaybackCompletedEvent;
import com.andressantibanez.spotifystreamer.tracksplayback.events.TrackPlayingProgressEvent;

import kaaes.spotify.webapi.android.models.Track;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = BaseActivity.class.getSimpleName();

    protected MenuItem mNowPlayingMenuItem;
    protected MenuItem mShareCurrentTrackMenuItem;
    protected MenuItem mPreferencesMenuItem;

    /**
     * Menu methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activities, menu);

        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing);
        mShareCurrentTrackMenuItem = menu.findItem(R.id.action_share_current_track);
        mPreferencesMenuItem = menu.findItem(R.id.action_edit_preferences);

        mNowPlayingMenuItem.setVisible(false);
        mShareCurrentTrackMenuItem.setVisible(false);
        mPreferencesMenuItem.setVisible(displayPreferencesMenuItem());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_share_current_track) {
            shareCurrentTrack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean displayNowPlayingMenuItem() {
        return true;
    }

    public boolean displayPreferencesMenuItem() {
        return true;
    }

    /**
     * Share current track
     */
    public void shareCurrentTrack() {
        SpotifyStreamerApp app = (SpotifyStreamerApp) getApplication();

        Track currentTrack = app.getCurrentTrack();
        if(currentTrack == null)
            return;

        String shareCurrentTrackText = "I'm listening to " + currentTrack.name + " by " + currentTrack.artists.get(0).name + " via Spotify Streamer App";
        String externalUrl = currentTrack.external_urls.get("spotify");
        if(externalUrl != null)
            shareCurrentTrackText += " (" + externalUrl + ")";

        Log.d(TAG, shareCurrentTrackText);

        Intent shareTrackIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareTrackIntent.setType("text/plain");
        shareTrackIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareCurrentTrackText);
        startActivity(Intent.createChooser(shareTrackIntent, getString(R.string.share_using)));
    }


    /**
     * Event handling
     */
    public void onEventMainThread(TrackPlayingProgressEvent event) {
        mNowPlayingMenuItem.setVisible(displayNowPlayingMenuItem());
        mShareCurrentTrackMenuItem.setVisible(true);
    }

    public void onEventMainThread(TrackPlaybackCompletedEvent event) {
        mNowPlayingMenuItem.setVisible(false);
        mShareCurrentTrackMenuItem.setVisible(false);
    }
}
