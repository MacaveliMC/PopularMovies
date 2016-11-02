/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * The main activity when the device starts up.  Displays the fragment with the movie grid.
 */
public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback, DetailFragment.CallbackDetail, ReviewFragment.CallbackReview {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String REVIEWFRAGMENT_TAG = "RFTAG";
    private final String TRAILERFRAGMENT_TAG = "TFTAG";

    /**
     * Sets the layout to activity_main
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            Log.v(LOG_TAG, "TWO PANE IS TRUE");
            mTwoPane = true;
            if (savedInstanceState == null) {

                DetailFragment dFragment = new DetailFragment();
                Bundle dArguments = new Bundle();
                dArguments.putBoolean(DetailFragment.TWO_PANE, true);
                dFragment.setArguments(dArguments);
                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, dFragment, DETAILFRAGMENT_TAG).commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }


    /**
     * Callback method when an item is selected on the gridview in the main fragment
     *
     * @param contentUri
     */
    @Override
    public void onItemSelected(Uri contentUri, View view) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
            args.putBoolean(DetailFragment.TWO_PANE, true);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();

        } else {
                Intent intent = new Intent(this, DetailActivity.class)
                        .setData(contentUri);
                startActivity(intent);
        }
    }

    /**
     * Callack method when the arrow to the review screen on the detail screen is clicked
     *
     * @param movieUri
     */
    @Override
    public void reviewButtonSelected(Uri movieUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, movieUri);
            args.putBoolean(ReviewFragment.TWO_PANE, true);

            ReviewFragment fragment = new ReviewFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, REVIEWFRAGMENT_TAG)
                    .commit();
        }

    }

    /**
     * Callback method when the arrow to the detail screen on the review screen is clicked
     *
     * @param movieUri
     */
    @Override
    public void detailButtonSelected(Uri movieUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, movieUri);
            args.putBoolean(DetailFragment.TWO_PANE, true);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }

    /**
     * Callback method for the arrow to the trailer screen on the review screen is clicked
     *
     * @param movieUri
     */
    @Override
    public void trailerButtonSelected(Uri movieUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(TrailerFragment.TRAILER_URI, movieUri);
            args.putBoolean(TrailerFragment.TWO_PANE, true);

            TrailerFragment fragment = new TrailerFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, TRAILERFRAGMENT_TAG)
                    .commit();
        }
    }

    /**
     * Callback method for the arrow to the review screen from the trailer screen is clicked.
     * @param movieUri
     *
     @Override public void reviewButtonSelectedFromTrailer(Uri movieUri){
     if(mTwoPane) {
     Bundle args = new Bundle();
     args.putParcelable(ReviewFragment.REVIEW_URI, movieUri);

     ReviewFragment fragment = new ReviewFragment();
     fragment.setArguments(args);

     getSupportFragmentManager().beginTransaction()
     .replace(R.id.movie_detail_container, fragment, REVIEWFRAGMENT_TAG)
     .commit();
     }
     }*/

    /**
     * Creates the options menu from the menu_main file
     *
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
     *
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
