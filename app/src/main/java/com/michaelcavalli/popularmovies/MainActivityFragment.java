/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.michaelcavalli.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * The fragment for the main activity, containing the grid of movies.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private MovieAdapter mMovieAdapter;
    private Callback myCallBack;
    GridView movieLayout;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int MOVIE_LOADER = 0;

    // Columns to retrieve from the movie DB
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_ORDER
    };

    // Columns to retrieve from the favorites DB
    private static final String[] FAVORITE_COLUMNS = {
            MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry._ID,
            MovieContract.FavoriteEntry.COLUMN_POSTER_PATH,
    };

    // Pointers to String arrays
    static final int COL_MOVIE_ID = 0;
    static final int COL_POSTER_PATH = 1;
    static final int COL_ORDER = 2;


    /**
     * Lets the actionbar know the fragment contributes to the menu items.
     *
     * @param savedInstance
     */
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        myCallBack = (Callback) activity;
        super.onAttach(activity);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, View view);
    }

    /**
     * Used to declare the loader
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Inflates the menu options for the fragment
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    /**
     * Creates and assigns the custom movie adapter to the grid view layout. Also
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        // assigns the image adapter to the gridview
        movieLayout = (GridView) rootView.findViewById(R.id.movie_grid);
        movieLayout.setAdapter(mMovieAdapter);

        // the on click listener for the gridview
        movieLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * The onclick listener for the gridview.  Identifies movie clicked, creates the correct
             * url path, then sends that in an intent to the detail activity.
             * @param parent the image adapter involved
             * @param view
             * @param position the position in the grid view
             * @param id
             */
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                mPosition = position;

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    myCallBack.onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getInt(COL_MOVIE_ID)), view);
                }

            }
        });


        return rootView;
    }

    /**
     * Takes action based on menu item selected
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Refresh item refreshes grid of movies
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        } else

            return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a new fetch movie data task to get the latest information from the api.
     * First checks what the sort preference is, then sends it to the asynctask.
     */
    private void updateMovies() {
        FetchMovieData movieTask = new FetchMovieData();
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.sort_order_key), getString(R.string.order_pop_desc));
        movieTask.execute(sortOrder);
    }

    /**
     * Immediately updates movies with current sort preference upon start.
     */
    @Override
    public void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting())
            updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save position for rotating
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Retrieves the data from the API for the movie grid off of the UI thread
     */
    public class FetchMovieData extends AsyncTask<String, Void, String> {

        /**
         * Connects to the API and gathers the movie data, then returns a list of
         * movieObjects with all the movie data
         *
         * @param params
         * @return
         */
        @Override
        public String doInBackground(String... params) {

            if (params[0] == null) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Use the URI to build the URL string
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by", params[0])
                    .appendQueryParameter("api_key", getString(R.string.api_key));

            String movieJSON = null;

            try {
                URL url = new URL(builder.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieJSON = buffer.toString();

            } catch (IOException e) {

            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, getString(R.string.error_closing_stream), e);
                    }
            }
            try {
                if (movieJSON != null)
                    return getMovieDataFromJSON(movieJSON);
                else
                    return null;
            } catch (JSONException e) {
                Log.v(LOG_TAG, getString(R.string.json_exception) + e.toString());
            }
            return null;
        }

        /**
         * Clears the movie image adapter and adds the new data
         *
         * @param
         */
        @Override
        protected void onPostExecute(String result) {

        }
    }

    /**
     * Takes the JSON and extracts all movie data, inserting into movieObjects.
     *
     * @param movieJSON
     * @return list of movieObjects
     * @throws JSONException
     */
    private String getMovieDataFromJSON(String movieJSON) throws JSONException {

        String pathStart = getString(R.string.path_start);

        JSONObject movieJsonObject = new JSONObject(movieJSON);
        JSONArray movieJsonList = movieJsonObject.getJSONArray(getString(R.string.results_array));

        Vector<ContentValues> cVVector = new Vector<ContentValues>(movieJsonList.length());

        for (int i = 0; i < movieJsonList.length(); i++) {
            JSONObject movie = movieJsonList.getJSONObject(i);
            int number = i;
            String title = movie.getString(getString(R.string.original_title));
            String path = movie.getString(getString(R.string.poster_path));
            String overview = movie.getString(getString(R.string.overview));
            String releaseDate = movie.getString(getString(R.string.release_date));
            String movieId = movie.getString(getString(R.string.id));
            String voteAverage = movie.getString(getString(R.string.vote_average));
            path = pathStart + path;


            // New Database storage method
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, title);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, path);
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(MovieContract.MovieEntry._ID, movieId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_ORDER, number);
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
            movieValues.put(MovieContract.MovieEntry.COLUMN_SORT, PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("order", getString(R.string.order_pop_desc)));

            cVVector.add(movieValues);
        }


        // delete old data so we don't build up an endless history
        String[] selectArgs = new String[]{PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("order", getString(R.string.order_pop_desc))};
        getActivity().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_SORT + " = ?", selectArgs);

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getActivity().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
        }

        return "Movie Download Complete. " + cVVector.size() + " Inserted";
    }

    /**
     * Returns the cursor loader for the movies that should be loaded into the grid, based on sort.
     *
     * @param i
     * @param bundle
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.
        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order: Ascending, date.
        String sortSetting = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("order", getString(R.string.order_pop_desc));

        String sortOrder;
        Uri searchUri;
        if (sortSetting.equals(getString(R.string.order_favorites))) {
            sortOrder = null;
            searchUri = MovieContract.FavoriteEntry.CONTENT_URI;
            return new CursorLoader(getActivity(),
                    searchUri,
                    FAVORITE_COLUMNS,
                    null,
                    null,
                    sortOrder);
        } else {
            sortOrder = MovieContract.MovieEntry.COLUMN_ORDER + " ASC";
            searchUri = MovieContract.MovieEntry.buildMovieWithSort(sortSetting);
            return new CursorLoader(getActivity(),
                    searchUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }
    }

    /**
     * Swaps in the correct cursor into the adapter.  Also may scroll to a previous position.
     *
     * @param cursorLoader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            movieLayout.smoothScrollToPosition(mPosition);
        }
    }

    /**
     * Changes the adapter cursor to null
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}


