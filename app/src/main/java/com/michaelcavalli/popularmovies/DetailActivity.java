/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Detail Activity for the detail screen which appears upon clicking on a movie poster from the main activity
 */
public class DetailActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    private DetailFragment dFragment;
    private ReviewFragment rFragment;
    private TrailerFragment tFragment;
    private ViewPager vPager;


    /**
     * Loads the xml layout and adds the main activity fragment to it
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Create bundles for each fragment and add URI to each
        Bundle Darguments = new Bundle();
        Bundle Rarguments = new Bundle();
        Bundle Targuments = new Bundle();
        Darguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
        Rarguments.putParcelable(ReviewFragment.REVIEW_URI, getIntent().getData());
        Targuments.putParcelable(TrailerFragment.TRAILER_URI, getIntent().getData());

        // Create detail fragment
        dFragment = new DetailFragment();
        dFragment.setArguments(Darguments);

        // Create review fragment
        rFragment = new ReviewFragment();
        rFragment.setArguments(Rarguments);

        // Create trailer fragment
        tFragment = new TrailerFragment();
        tFragment.setArguments(Targuments);

        // Viewpager that allows swiping between the three fragments - so cool!
        vPager = (ViewPager) findViewById(R.id.viewPager);
        vPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        vPager.setOnPageChangeListener(this);
    }

    /**
     * Pager adapter class for filling the viewpager with the fragments
     */
    private class MyPagerAdapter extends FragmentPagerAdapter {

        /**
         * Constructor
         * @param fm
         */
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Returns the fragments to be used for each position
         * @param pos Position being asekd for
         * @return
         */
        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return dFragment;
                case 1: return rFragment;
                case 2: return tFragment;
                default: return new Fragment();
            }
        }

        /**
         * Returns the total number of fragments in the viewpager
         * @return
         */
        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * Changes the title in the action bar when you change fragments
     * @param position
     */
    @Override
    public void onPageSelected(int position) {

        Log.v(LOG_TAG, "onPageSelected with position: " + position);

        switch(position){
            case 0: {
                getSupportActionBar().setTitle(getString(R.string.details_nav));
                break;
            }
            case 1: {
                getSupportActionBar().setTitle(getString(R.string.reviews_nav));
                break;
            }
            case 2:{
                getSupportActionBar().setTitle(getString(R.string.trailers_nav));
                break;
            }
            default: Log.v(LOG_TAG, "DEFAULT PAGE");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
