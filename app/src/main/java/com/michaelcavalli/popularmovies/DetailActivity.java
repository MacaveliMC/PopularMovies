/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Detail Activity for the detail screen which appears upon clicking on a movie poster from the main activity
 */
public class DetailActivity extends ActionBarActivity {

    /**
     * Loads the xml layout and adds the main activity fragment to it
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new DetailFragment()).commit(); //
        }
    }

    /**
     * This is the detail fragment that loads up an displays all the information on the clicked movie
     */
    public static class DetailFragment extends Fragment {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private String movieUrl;
        View rootView;

        /**
         * Creates the layout based on screen orientation, and starts the asynctask to get the movie
         * info based on the extra in the intent that started the parent activity.
         * @param inflater
         * @param container
         * @param savedInstanceState
         * @return
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Intent startIntent = getActivity().getIntent();

            WindowManager mWindowManager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
            Display mDisplay = mWindowManager.getDefaultDisplay();

            if(mDisplay.getRotation() == Surface.ROTATION_0)
                rootView = inflater.inflate(R.layout.fragment_detail_portrait, container, false);
            else
                rootView = inflater.inflate(R.layout.fragment_detail_landscape, container, false);

            if (startIntent != null && startIntent.hasExtra(Intent.EXTRA_TEXT)) {
                movieUrl = startIntent.getStringExtra(Intent.EXTRA_TEXT);
                FetchMovieDetail clickedMovieDetail = new FetchMovieDetail();
                clickedMovieDetail.execute(movieUrl);
            }

            return rootView;
        }

        /**
         * Fetches the details of the clicked movie from the movie API using the passed url
         * in param[0]
         */
        public class FetchMovieDetail extends AsyncTask<String, Void, MovieObject> {

            /**
             * Background task to fetch data outside of the UI thread
             */
            @Override
            public MovieObject doInBackground(String... params) {

                if (params[0] == null)
                    return null;

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String movieDetailJSON = null;

                try {
                    URL url = new URL(params[0]);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod(getString(R.string.urlConnection_request_method));
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
                    movieDetailJSON = buffer.toString();

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
                    return getMovieDetailFromJSON(movieDetailJSON);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, getString(R.string.json_exception) + e.toString());
                    return null;
                }
            }

            /**
             * Makes changes based on data returned from background thread
             * @param finalResult the result returned by the background task
             */
            @Override
            protected void onPostExecute(MovieObject finalResult) {
                ImageView poster = (ImageView)rootView.findViewById(R.id.detail_picture);
                TextView title = (TextView)rootView.findViewById(R.id.movie_title_view);
                TextView release = (TextView)rootView.findViewById(R.id.release_date);
                TextView vote = (TextView)rootView.findViewById(R.id.vote_average);
                TextView overview = (TextView)rootView.findViewById(R.id.movie_overview);

                Picasso.with(getActivity()).load(finalResult.getMoviePosterPath()).into(poster);
                title.setText(finalResult.getMovieTitle());
                release.setText(finalResult.getMovieReleaseDate());
                vote.setText(finalResult.getMovieVoteAverage());
                overview.setText(finalResult.getMovieOverview());
            }

        }

        /**
         * Takes the JSON string and retrieves the movie details that we need for the detail view
         * @param movieDetailJSON JSON string to use
         * @return the movieObject with all our movie information we need
         * @throws JSONException
         */
        public MovieObject getMovieDetailFromJSON(String movieDetailJSON) throws JSONException {
            MovieObject movieClicked;
            String pathStart = "http://image.tmdb.org/t/p/w185/";

            JSONObject movie = new JSONObject(movieDetailJSON);

            String title = movie.getString(getString(R.string.original_title));
            String path = movie.getString(getString(R.string.poster_path));
            String overview = movie.getString(getString(R.string.overview));
            String releaseDate = movie.getString(getString(R.string.release_date));
            String movieId = movie.getString(getString(R.string.id));
            String voteAverage = movie.getString(getString(R.string.vote_average));
            path = pathStart + path;

            movieClicked = new MovieObject(title, path, overview, releaseDate, movieId, voteAverage);

            return movieClicked;
        }

    }

}
