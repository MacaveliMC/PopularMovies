/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ReviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ReviewFragment.class.getSimpleName();
    static final String REVIEW_URI = "URI";
    static final String TWO_PANE = "TWO_PANE";
    private Uri reviewUri;
    ListView reviewList;
    String movieID;
    ReviewAdapter mReviewAdapter;
    ContentResolver myContentResolver;
    View rootView;
    private boolean mTwoPane;


    // Callback to detail activity for single pane
    private CallBackInterface myCallBack;
    // Callback to main activity for two pane
    private CallbackReview callBackReview;

    // Loader number
    private static final int REVIEW_LOADER = 1;

    // Columns to return from the DB
    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.COLUMN_ORDER
    };

    static final int COL_ID = 0;
    static final int COL_AUTHOR = 1;
    static final int COL_CONTENT = 2;
    static final int COL_ORDER = 3;

    ImageView toDetails;
    ImageView toTrailers;


    /**
     * Set up the layout of the fragment
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
            reviewUri = arguments.getParcelable(REVIEW_URI);
            mTwoPane = arguments.getBoolean(TWO_PANE);
        }

        if (mTwoPane) {
            try {
                callBackReview = (CallbackReview) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + " must implement CallBackDetail interface!");
            }
        } else {
            try {
                myCallBack = (CallBackInterface) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString() + " must implement CallBackInterface!");
            }
        }

        myContentResolver = getActivity().getContentResolver();
        movieID = MovieContract.MovieEntry.getIdFromUri(reviewUri);

        rootView = inflater.inflate(R.layout.review_fragment, container, false);
        rootView.setVisibility(View.INVISIBLE);

        toDetails = (ImageView) rootView.findViewById(R.id.go_to_details_from_reviews);
        toTrailers = (ImageView) rootView.findViewById(R.id.go_to_trailers_from_reviews);

        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
        reviewList = (ListView) rootView.findViewById(R.id.listview_reviews);
        reviewList.setAdapter(mReviewAdapter);

        // Create a click listener for the arrow going to the details fragment
        toDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    callBackReview.detailButtonSelected(reviewUri);
                } else {
                    myCallBack.clickOnDetails();
                }
            }
        });

        // Create a click listener for the arrow going to the trailers fragment
        toTrailers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    callBackReview.trailerButtonSelected(reviewUri);
                } else {
                    myCallBack.clickOnTrailers();
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
    public interface CallbackReview {
        /**
         * When the arrow is clicked to go to the detail screen
         */
        public void detailButtonSelected(Uri movieUri);

        public void trailerButtonSelected(Uri movieUri);
    }


    /**
     * Initiates the loader
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Returns the cursorloader for the adapter
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MovieContract.ReviewEntry.COLUMN_ORDER + " ASC";
        Uri reviewsForMovie = MovieContract.ReviewEntry.buldReviewWithMovie(movieID);

        return new CursorLoader(getActivity(),
                reviewsForMovie,
                REVIEW_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * Swaps out the cursor for the adapter
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mReviewAdapter.swapCursor(data);
        rootView.setVisibility(View.VISIBLE);
    }

    /**
     * Swap out the cursor in the adapter for null
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mReviewAdapter.swapCursor(null);
    }

    /**
     * Creates a new fetch movie data task to get the latest information from the api.
     * First checks what the sort preference is, then sends it to the asynctask.
     */
    private void updateReviews() {
        FetchReviewData reviewTask = new FetchReviewData();
        reviewTask.execute();
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
            updateReviews();
    }

    /**
     * Retrieves the data from the API for the movie grid off of the UI thread
     */
    public class FetchReviewData extends AsyncTask<Void, Void, String> {


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

            // Use the URI to build the URL string
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(movieID)
                    .appendPath("reviews")
                    .appendQueryParameter("api_key", getString(R.string.api_key));


            String reviewJSON = null;

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
                reviewJSON = buffer.toString();

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
                return getReviewDataFromJSON(reviewJSON);
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
     * @param reviewJSON
     * @return list of movieObjects
     * @throws JSONException
     */
    private String getReviewDataFromJSON(String reviewJSON) throws JSONException {

        JSONObject reviewJsonObject = new JSONObject(reviewJSON);
        JSONArray reviewJsonList = reviewJsonObject.getJSONArray(getString(R.string.results_array));

        Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewJsonList.length());

        for (int i = 0; i < reviewJsonList.length(); i++) {
            JSONObject movie = reviewJsonList.getJSONObject(i);
            int number = i;
            String author = movie.getString(getString(R.string.author));
            String content = movie.getString(getString(R.string.content));


            // New Database storage method
            ContentValues movieValues = new ContentValues();


            movieValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, author);
            movieValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, content);
            movieValues.put(MovieContract.ReviewEntry.COLUMN_ORDER, number);
            movieValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieID);

            cVVector.add(movieValues);
        }


        // delete old data so we don't build up an endless history
        String[] selectArgs = new String[]{movieID};
        if (myContentResolver != null)
            myContentResolver.delete(MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?", selectArgs);

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            if (myContentResolver != null)
                myContentResolver.bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
        }

        return "Review Download Complete. " + cVVector.size() + " Inserted";
    }

    public interface CallBackInterface {
        public void clickOnDetails();

        public void clickOnTrailers();
    }
}
