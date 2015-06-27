package com.andressantibanez.spotifystreamer.artisttopsongs;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.tracksplayback.PlaybackService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class ArtistTopTracksFragment extends Fragment {

    //Constants
    private static final String ARTIST_ID = "artist_id";
    private static final String ARTIST_NAME = "artist_name";
    private static final String TOP_TRACKS_RESULTS = "top_tracks_results";
    private static final String SEARCH_DONE = "search_done";

    //Variables
    boolean mSearchDone;
    String mArtistId;
    String mArtistName;
    List<Track> mTopTracksList;
    TopTracksAdapter mAdapter;
    GetArtistTopTracksTask mCurrentTask;
    InteractionListener mListener;

    //Controls
    @InjectView(R.id.top_tracks_list) ListView mTopTracksListView;
    @InjectView(R.id.throbber) ProgressBar mProgressBar;

    /**
     * Factory method
     */
    public static ArtistTopTracksFragment newInstance(String artistId, String artistName) {
        ArtistTopTracksFragment fragment = new ArtistTopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARTIST_ID, artistId);
        args.putString(ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required empty constructor
     */
    public ArtistTopTracksFragment() {}

    /**
     * Lifecycle methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtistId = getArguments().getString(ARTIST_ID);
            mArtistName = getArguments().getString(ARTIST_NAME);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        cancelSearch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_top_tracks, container, false);
        ButterKnife.inject(this, view);

        mTopTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Track track = mAdapter.getItem(position);
                mListener.onTrackSelected(mTopTracksList, track.id);
            }
        });

        restoreState(savedInstanceState);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (InteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InterationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mSearchDone)
            searchTopTracks();
        else
            hideLoading();
    }


    /**
     * State handling
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOP_TRACKS_RESULTS, new Gson().toJson(mTopTracksList));
        outState.putBoolean(SEARCH_DONE, mSearchDone);
    }

    public void restoreState(Bundle savedInstanceState) {
        if(savedInstanceState == null)
            return;

        mSearchDone = savedInstanceState.getBoolean(SEARCH_DONE);

        String jsonTopTracks = savedInstanceState.getString(TOP_TRACKS_RESULTS);
        if(jsonTopTracks != null) {

            Type type = new TypeToken<List<Track>>(){}.getType();
            mTopTracksList = new Gson().fromJson(jsonTopTracks, type);

            if(mTopTracksList == null)
                mTopTracksList = new ArrayList<>();

            showTopTracks();
        }
    }


    /**
     * Task methods
     */
    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void searchTopTracks() {
        cancelSearch();

        mCurrentTask = new GetArtistTopTracksTask();
        mCurrentTask.execute(mArtistId);
    }

    private void cancelSearch() {
        if(mCurrentTask == null)
            return;

        mCurrentTask.cancel(false);
    }

    class GetArtistTopTracksTask extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... strings) {
            String artistId = strings[0];

            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();
                Tracks tracks = spotifyService.getArtistTopTrack(artistId, options);
                return tracks.tracks;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Track> tracksList) {
            hideLoading();

            //Network connection. Tracks is null.
            if(tracksList == null) {
                Toast.makeText(getActivity(), R.string.network_error_try_again, Toast.LENGTH_LONG).show();
                return;
            }

            //No Top Tracks found.
            if(tracksList.size() == 0) {
                Toast.makeText(getActivity(), R.string.no_top_tracks_found, Toast.LENGTH_LONG).show();
                return;
            }

            //Show Top Tracks.
            mSearchDone = true;
            mTopTracksList = tracksList;
            showTopTracks();
        }

    }

    private void showTopTracks() {
        mAdapter = new TopTracksAdapter(getActivity());
        mAdapter.setTopTracksList(mTopTracksList);
        mTopTracksListView.setAdapter(mAdapter);
    }

    /**
     * InteractionListener interface
     */
    public interface InteractionListener {
        public void onTrackSelected(List<Track> tracksList, String trackId);
    }

}
