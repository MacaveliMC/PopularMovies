/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.michaelcavalli.popularmovies.data.MovieContract;

import org.json.JSONArray;
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
 * Created by silen_000 on 11/1/2015.
 */
public class TrailerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = TrailerFragment.class.getSimpleName();
    static final String TRAILER_URI = "URI";
    static final String TWO_PANE = "TWO_PANE";
    private Uri trailerUri;
    ListView trailerList;
    String movieID;
    TrailerAdapter mTrailerAdapter;
    ContentResolver myContentResolver;
    private ShareActionProvider mShareActionProvider;
    View rootView;
    private boolean mTwoPane;

    // Callback to detail activity for single pane
    private DetailFragment.CallBackInterface myCallBack;
    // Callback to main activity for two pane
    private DetailFragment.CallbackDetail callBackTrailer;

    // Loader number
    private static final int TRAILER_LOADER = 2;

    // Columns to return from the DB
    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_NAME,
            MovieContract.TrailerEntry.COLUMN_MOVIE_ID,
            MovieContract.TrailerEntry.COLUMN_KEY,
            MovieContract.TrailerEntry.COLUMN_ORDER
    };

    static final int COL_ID = 0;
    static final int COL_NAME = 1;
    static final int COL_MOVIE_ID = 2;
    static final int COL_KEY = 3;
    static final int COL_ORDER = 4;

    ImageView toReviews;


    /**
     * Creating the layout for the fragment
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            trailerUri = arguments.getParcelable(TRAILER_URI);
            mTwoPane = arguments.getBoolean(TWO_PANE);
        }

        if (mTwoPane) {
            try {
                callBackTrailer = (DetailFragment.CallbackDetail) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + " must implement CallBackDetail interface!");
            }
        } else {
            try {
                myCallBack = (DetailFragment.CallBackInterface) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + " must implement CallBackInterface!");
            }
        }


        myContentResolver = getActivity().getContentResolver();
        movieID = MovieContract.MovieEntry.getIdFromUri(trailerUri);

        rootView = inflater.inflate(R.layout.trailer_fragment, container, false);
        rootView.setVisibility(View.INVISIBLE);

        toReviews = (ImageView) rootView.findViewById(R.id.go_to_reviews_from_trailers);

        mTrailerAdapter = new TrailerAdapter(getActivity(), null, 0);
        trailerList = (ListView) rootView.findViewById(R.id.listview_trailers);
        trailerList.setAdapter(mTrailerAdapter);

        // Set the click listener for the arrow going back to the reviews screen on tablets
        toReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    callBackTrailer.reviewButtonSelected(trailerUri);
                } else {
                    myCallBack.clickOnReviews();
                }
            }
        });

        // Set up the click listener for the listview
        trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String key = cursor.getString(COL_KEY);
                String path = getString(R.string.youtube_intent_path);
                String queryParam = "v";
                Uri ytUri = Uri.parse(path).buildUpon()
                        .appendQueryParameter(queryParam, key).build();

                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
                youtubeIntent.setData(ytUri);


                if (youtubeIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(youtubeIntent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + ytUri.toString() + ", no apps installdd!");
                }

            }
        });

        return rootView;
    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface CallbackReviewFromTrailer {
        /**
         * Callback method for the arrow to go back to the review screen.
         */
        public void reviewButtonSelectedFromTrailer(Uri movieUri);
    }

    /**
     * Initiate the loader
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Return the cursor loader for the adapter
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MovieContract.TrailerEntry.COLUMN_ORDER + " ASC";
        Uri reviewsForMovie = MovieContract.TrailerEntry.buildTrailerWithMovie(movieID);

        return new CursorLoader(getActivity(),
                reviewsForMovie,
                TRAILER_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * Swap the cursor into the adapter to load the info
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTrailerAdapter.swapCursor(data);
        rootView.setVisibility(View.VISIBLE);
    }

    /**
     * Swap the cursor in the adapter for null
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrailerAdapter.swapCursor(null);
    }

    /**
     * Creates a new fetch movie data task to get the latest information from the api.
     * First checks what the sort preference is, then sends it to the asynctask.
     */
    private void updateTrailers() {
        FetchTrailerData trailerTask = new FetchTrailerData();
        trailerTask.execute();
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
            updateTrailers();
    }

    /**
     * Retrieves the data from the API for the movie grid off of the UI thread
     */
    public class FetchTrailerData extends AsyncTask<Void, Void, String> {


        /**
         * Connects to the API and gathers the movie data, then returns a list of
         * movieObjects with all the movie data
         *
         * @param params
         * @return
         */
        @Override
        public String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(movieID)
                    .appendPath("videos")
                    .appendQueryParameter("api_key", getString(R.string.api_key));

            String trailerJSON = null;

            try {
                URL url = new URL(builder.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
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
                trailerJSON = buffer.toString();

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
                return getTrailerDataFromJSON(trailerJSON);
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

            Log.v(LOG_TAG, result);

        }
    }

    /**
     * Takes the JSON and extracts all movie data, inserting into movieObjects.
     *
     * @param trailerJSON
     * @return list of movieObjects
     * @throws JSONException
     */
    private String getTrailerDataFromJSON(String trailerJSON) throws JSONException {

        JSONObject trailerJsonObject = new JSONObject(trailerJSON);
        JSONArray trailerJsonList = trailerJsonObject.getJSONArray(getString(R.string.results_array));

        Vector<ContentValues> cVVector = new Vector<ContentValues>(trailerJsonList.length());

        for (int i = 0; i < trailerJsonList.length(); i++) {
            JSONObject movie = trailerJsonList.getJSONObject(i);
            int number = i;
            String name = movie.getString(getString(R.string.trailer_name));
            String key = movie.getString(getString(R.string.trailer_key));

            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.TrailerEntry.COLUMN_NAME, name);
            movieValues.put(MovieContract.TrailerEntry.COLUMN_KEY, key);
            movieValues.put(MovieContract.TrailerEntry.COLUMN_ORDER, number);
            movieValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieID);

            cVVector.add(movieValues);
        }


        // delete old data so we don't build up an endless history
        String[] selectArgs = new String[]{movieID};
        if (myContentResolver != null)
            myContentResolver.delete(MovieContract.TrailerEntry.CONTENT_URI,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?", selectArgs);

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            if (myContentResolver != null)
                myContentResolver.bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
        }

        return "Review Download Complete. " + cVVector.size() + " Inserted";
    }


}
