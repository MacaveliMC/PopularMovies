/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;

/**
 * Settings activity with the sort preference
 */
public class SettingsActivity extends ActionBarActivity {

    /**
     * Creates the layout for settings
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_main);
    }


}
