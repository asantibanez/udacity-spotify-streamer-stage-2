package com.andressantibanez.spotifystreamer.artistsearch;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.artisttopsongs.ArtistTopTracksActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class ArtistSearchFragment extends Fragment {

    public static final String TAG = ArtistSearchFragment.class.getSimpleName();

    //Constants
    public static final String SEARCH_RESULTS = "search_results";
    public static final int SEARCH_DELAY = 500;

    //Variables
    SearchArtistTask mCurrentTask;
    ArtistSearchResultsAdapter mAdapter;
    List<Artist> mArtistsList;
    InteractionListener mListener;

    //Controls
    @InjectView(R.id.search_input) EditText mSearchInput;
    @InjectView(R.id.search_results) ListView mSearchResultsListView;
    @InjectView(R.id.throbber) ProgressBar mProgressBar;

    /**
     * Factory method
     */
    public static ArtistSearchFragment newInstance() {
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required constructor
     */
    public ArtistSearchFragment() {
    }

    /**
     * Lifecycle methods
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);
        ButterKnife.inject(this, view);

        setupControlsListeners();

        restoreState(savedInstanceState);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        cancelSearch();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (InteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * State handling
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String jsonSearchResults = new Gson().toJson(mArtistsList);
        outState.putString(SEARCH_RESULTS, jsonSearchResults);
    }

    public void restoreState(Bundle savedInstanceState) {
        if(savedInstanceState == null)
            return;

        String jsonSearchResults = savedInstanceState.getString(SEARCH_RESULTS);
        if(jsonSearchResults != null) {

            Type type = new TypeToken<List<Artist>>() {}.getType();
            mArtistsList = new Gson().fromJson(jsonSearchResults, type);

            if(mArtistsList == null)
                mArtistsList = new ArrayList<>();

            showResults();
        }
    }

    /**
     * Custom methods
     */
    public void setupControlsListeners() {
        //Configure search input
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchArtists();
            }
        });

        //Item click for ListView
        mSearchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Artist artist = mAdapter.getItem(position);
                mListener.onArtistSelected(artist.id, artist.name);
            }
        });
    }

    private void showResults() {
        mAdapter = new ArtistSearchResultsAdapter(getActivity());
        mAdapter.setArtistsList(mArtistsList);
        mSearchResultsListView.setAdapter(mAdapter);
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Artist search methods
     */
    private void searchArtists() {
        cancelSearch();

        showLoading();
        mCurrentTask = new SearchArtistTask();
        mCurrentTask.execute(mSearchInput.getText().toString());
    }

    private void cancelSearch() {
        if(mCurrentTask == null)
            return;
        mCurrentTask.cancel(false);
    }

    class SearchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... strings) {
            //Get search input
            String queryString = strings[0];

            //Delay search if user is typing
            try {
                Thread.sleep(SEARCH_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Check if task cancelled
            if(isCancelled() || queryString.length() == 0)
                return new ArrayList<Artist>();

            //Add wildcards for LIKE type seach
            queryString = "*" + queryString + "*";

            //Call Spotify Api
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();
                ArtistsPager artistsPager = spotifyService.searchArtists(queryString);
                return artistsPager.artists.items;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(List<Artist> artistsList) {
            hideLoading();

            //Network error. ArtistsList is null.
            if(artistsList == null) {
                Toast.makeText(getActivity(), R.string.network_error_try_again, Toast.LENGTH_LONG).show();
                return;
            }

            //No Artists found. List has zero items and search input length greater than zero
            if(artistsList.size() == 0 && mSearchInput.getText().toString().length() > 0) {
                Toast.makeText(getActivity(), R.string.no_artists_found, Toast.LENGTH_LONG).show();
            }

            //Get artists
            mArtistsList = artistsList;
            showResults();
        }
    }


    /**
     * InteractionListener interface
     */
    public interface InteractionListener {
        public void onArtistSelected(String artistId, String artistName);
    }

}
