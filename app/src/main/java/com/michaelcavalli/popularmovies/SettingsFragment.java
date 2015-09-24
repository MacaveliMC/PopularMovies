/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Fragment for the settings activity
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private final String LOG_TAG = SettingsFragment.class.getSimpleName();

    /**
     * Constructor.  Not currently used.
     */
    public SettingsFragment(){

    }

    /**
     * Loads the current preferences from the xml file
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);


        bindPreferenceSummaryToValue(findPreference(getString(R.string.sort_order_key)));

    }


    /**
     * Binds the preference summary to the current value selected
     * @param preference
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     *
     * @param preference
     * @param value
     * @return
     */
    public boolean onPreferenceChange(Preference preference, Object value) {
        String sValue = value.toString();

        ListPreference listPreference = (ListPreference) preference;
        int prefIndex = listPreference.findIndexOfValue(sValue);
        if (prefIndex >= 0) {
            preference.setSummary(listPreference.getEntries()[prefIndex]);

        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(sValue);
        }

        return true;
    }
}
