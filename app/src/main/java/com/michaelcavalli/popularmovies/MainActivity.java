/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The main activity when the device starts up.  Displays the fragment with the movie grid.
 */
public class MainActivity extends ActionBarActivity {

    /**
     * Sets the layout to activity_main
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Creates the options menu from the menu_main file
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Decides what to do when a menu option is selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            // Starts the settings menu activity
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
