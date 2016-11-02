/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.michaelcavalli.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * This is the detail fragment that loads up an displays all the information on the clicked movie
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";     // For retrieving the sent URI
    static final String TWO_PANE = "TWO_PANE";  // For retrieving the info on two pane mode
    private Uri movieUri;                       // The URI for this movie
    private boolean mTwoPane;                   // true if in two pane mode
    private boolean isFavorite;                 // True if this movie is in favorites
    View rootView;

    // Callback to detail activity for single pane
    private CallBackInterface myCallBack;
    // Callback to main activity for two pane
    private CallbackDetail callBackDetail;

    // Loader number
    private static final int DETAIL_LOADER = 0;

    // Columns to return for the movie details
    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_ORDER
    };

    // Pointers to the columns for movie details
    static final int COL_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_POSTER_PATH = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_RELEASE_DATE = 4;
    static final int COL_VOTE_AVG = 5;
    static final int COL_ORDER = 6;

    // Elements to fill
    ImageView poster;
    TextView title;
    TextView release;
    TextView vote;
    TextView overview;
    ImageView reviewbutton;
    ImageView favoriteStar;

    // Data from SQLite database
    String poster_path;
    String title_text;
    String release_text;
    String vote_text;
    String overview_text;
    String movieId;
    String order;

    /**
     * Creates the layout based on screen orientation, and starts the asynctask to get the movie
     * info based on the extra in the intent that started the parent activity.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            Log.v(LOG_TAG, "GETTING ARGUMENTS");
            movieUri = arguments.getParcelable(DETAIL_URI);
            mTwoPane = arguments.getBoolean(TWO_PANE);
        }

        // If we're in two pane mode, used the callBackDetail interface
        if (mTwoPane) {
            try {
                callBackDetail = (CallbackDetail) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + " must implement CallBackDetail interface!");
            }
        }
        // If we're in one pane mode, use the myCallBack interface
        else {
            try {
                myCallBack = (CallBackInterface) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + " must implement CallBackInterface!");
            }
        }

        // Find the rootview and make it invisible until we're done loading
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        rootView.setVisibility(View.INVISIBLE);

        // Save references to all items
        poster = (ImageView) rootView.findViewById(R.id.detail_picture);
        title = (TextView) rootView.findViewById(R.id.movie_title_view);
        release = (TextView) rootView.findViewById(R.id.release_date);
        vote = (TextView) rootView.findViewById(R.id.vote_average);
        overview = (TextView) rootView.findViewById(R.id.movie_overview);
        favoriteStar = (ImageView) rootView.findViewById(R.id.star);
        reviewbutton = (ImageView) rootView.findViewById(R.id.go_to_reviews);


        // Button to get to review fragment on tablets
        reviewbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    callBackDetail.reviewButtonSelected(movieUri);
                } else {
                    myCallBack.clickOnReviews();
                }
            }
        });

        // Set up the star to record the movie as a favorite, or delete it
        favoriteStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If it's already a favorite, delete it
                if (isFavorite) {
                    getActivity().getContentResolver().delete(MovieContract.FavoriteEntry.CONTENT_URI,
                            MovieContract.FavoriteEntry._ID + " = ?", new String[]{movieId});
                    // Change the star
                    favoriteStar.setImageResource(R.drawable.ic_star_border_white_24dp);
                }
                // If not then add it
                else {
                    ContentValues movieValues = new ContentValues();

                    movieValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_TITLE, title_text);
                    movieValues.put(MovieContract.FavoriteEntry.COLUMN_POSTER_PATH, poster_path);
                    movieValues.put(MovieContract.FavoriteEntry.COLUMN_OVERVIEW, overview_text);
                    movieValues.put(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE, release_text);
                    movieValues.put(MovieContract.FavoriteEntry._ID, movieId);
                    movieValues.put(MovieContract.FavoriteEntry.COLUMN_VOTE_AVERAGE, vote_text);

                    getActivity().getContentResolver().insert(MovieContract.FavoriteEntry.CONTENT_URI, movieValues);

                    // Change the star
                    favoriteStar.setImageResource(R.drawable.ic_star_border_white_24dp_selected);
                }
            }
        });
        return rootView;
    }


    /**
     * Allows user to get to review fragment on tablets (two pane mode)
     */
    public interface CallbackDetail {
        /**
         * Callback method implemented in activity
         */
        void reviewButtonSelected(Uri movieUri);
    }

    /**
     * Allows users to get to the review fragment in one pane mode
     */
    public interface CallBackInterface {
        void clickOnReviews();
    }

    /**
     * Set the loader when the activity starts
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * When the loader is created, get a cursor from the database pointing to the movie info
     *
     * @param i
     * @param bundle
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if (movieUri != null)
            return new CursorLoader(getActivity(),
                    movieUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        else
            return null;
    }

    /**
     * Use the cursor from the database to load all the movie information into the elements
     *
     * @param cursorLoader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            poster_path = data.getString(COL_POSTER_PATH);
            title_text = data.getString(COL_TITLE);
            release_text = data.getString(COL_RELEASE_DATE);
            vote_text = data.getString(COL_VOTE_AVG);
            overview_text = data.getString(COL_OVERVIEW);
            movieId = data.getString(COL_ID);


            /*
             * This set of code determines which star icon to use
             */
            Cursor c;
            String sortSetting = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("order", getString(R.string.order_pop_desc));
            // If the sort settings is favorites, movie has to be a favorite so select yellow star
            if (sortSetting.equals(getString(R.string.order_favorites))) {
                favoriteStar.setImageResource(R.drawable.ic_star_border_white_24dp_selected);
                isFavorite = true;
            }
            //Otherwise, determine if the movie is in the favorites database
            else {
                c = getActivity().getContentResolver().query(MovieContract.FavoriteEntry.CONTENT_URI,
                        new String[]{MovieContract.FavoriteEntry._ID},
                        MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry._ID + " = ?",
                        new String[]{movieId},
                        null);
                // If it is in the DB, set star yellow
                if (c.getCount() > 0) {
                    favoriteStar.setImageResource(R.drawable.ic_star_border_white_24dp_selected);
                    isFavorite = true;
                }
                // If not, set the star empty
                else {
                    favoriteStar.setImageResource(R.drawable.ic_star_border_white_24dp);
                    isFavorite = false;
                }
            }

            // Use picasso to set the image, set the rest of the elements
            Picasso.with(getActivity()).load(poster_path).into(poster);
            title.setText(title_text);
            release.setText(release_text);
            vote.setText(vote_text);
            overview.setText(overview_text);

            // After everything is loaded, make the rootview visible
            rootView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Not used
    }

}