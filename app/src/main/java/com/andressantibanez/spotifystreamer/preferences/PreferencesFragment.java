package com.andressantibanez.spotifystreamer.preferences;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.andressantibanez.spotifystreamer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String TAG = PreferencesFragment.class.getSimpleName();

    //Constants
    public static final String PREF_COUNTRY_FOR_RESULTS = "pref_country_for_results";

    //Variables
    SharedPreferences mSharedPreferences;

    public PreferencesFragment() {}

    public static Fragment newInstance() {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Lifecycle methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        updateCountryForResultsSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    /**
     * Custom methods
     */
    public void updateCountryForResultsSummary() {
        String currentValue = mSharedPreferences.getString(PREF_COUNTRY_FOR_RESULTS, "");
        List<String> countryEntryValues = Arrays.asList(getResources().getStringArray(R.array.pref_country_entryValues));
        List<String> countriesEntries = Arrays.asList(getResources().getStringArray(R.array.pref_country_entries));

        String summary = "";
        for(String entryValue : countryEntryValues) {
            if(entryValue.equals(currentValue)) {
                summary = countriesEntries.get(countryEntryValues.indexOf(entryValue));
                break;
            }
        }

        findPreference(PREF_COUNTRY_FOR_RESULTS).setSummary(summary);
    }


    /**
     * Listener for SharedPreferences update
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PREF_COUNTRY_FOR_RESULTS))
            updateCountryForResultsSummary();
    }

}
