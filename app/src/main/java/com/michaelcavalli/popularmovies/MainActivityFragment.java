/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

/**
 * The fragment for the main activity, containing the grid of movies.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private ImageAdapter movieImageAdapter;

    /**
     * Constructor for this fragment. Does nothing for now.
     */
    public MainActivityFragment() {
    }

    /**
     * Lets the actionbar know the fragment contributes to the menu items.
     * @param savedInstance
     */
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        setHasOptionsMenu(true);
    }

    /**
     * Inflates the menu options for the fragment
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    /**
     * Creates and assigns the custom movie adapter to the grid view layout. Also
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // creates the image adapter
        movieImageAdapter = new ImageAdapter(getActivity(),R.layout.movie_grid,R.id.grid_image, new ArrayList<MovieObject>());

        // assigns the image adapter to the gridview
        GridView movieLayout = (GridView) rootView.findViewById(R.id.movie_grid);
        movieLayout.setAdapter(movieImageAdapter);

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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieObject currentMovie = (MovieObject)movieImageAdapter.getItem(position);
                String movieInfoPath = getString(R.string.movie_detail_starter_path) + currentMovie.getMovieId() + "?api_key=" + getString(R.string.api_key);
                Intent startDetailActivity = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movieInfoPath);
                startActivity(startDetailActivity);
            }
        });

        return rootView;
    }

    /**
     * Takes action based on menu item selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Refresh item refreshes grid of movies
        if (id == R.id.action_refresh) {
            Log.v(LOG_TAG,"Refreshing movies");
            updateMovies();
            Log.v(LOG_TAG,"Movies Refreshed");
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
    public void onStart(){
        super.onStart();
        updateMovies();
    }

    /**
     * Custom array adapter to adapt the custom movieObject class to the gridview
     */
    public class ImageAdapter extends ArrayAdapter{
        private Context mContext;

        public ImageAdapter(Context context, int resource, int textViewResourceId, List<MovieObject> objects){
            super(context, resource, textViewResourceId, objects);
            mContext = context;
        }

        /**
         * Returns the imageview with the correct image in it for the position given
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ImageView view = (ImageView) convertView;
            if (view == null) {
                view = new ImageView(mContext);
            }
            MovieObject currentMovie;
            currentMovie = (MovieObject)getItem(position);
            String url = currentMovie.getMoviePosterPath();
            Log.v(LOG_TAG, "URL to load: " + url);
            Picasso.with(mContext).load(url).into(view);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setAdjustViewBounds(true);
            return view;
        }
    }

    /**
     * Retrieves the data from the API for the movie grid off of the UI thread
     */
    public class FetchMovieData extends AsyncTask<String, Void, MovieObject[]>{

        /**
         * Connects to the API and gathers the movie data, then returns a list of
         * movieObjects with all the movie data
         * @param params
         * @return
         */
        @Override
        public MovieObject[] doInBackground(String... params){

            if(params[0] == null){
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

            try{
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
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJSON = buffer.toString();

            } catch (IOException e){

            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
                if(reader != null)
                    try{
                        reader.close();
                    } catch (IOException e){
                        Log.e(LOG_TAG, getString(R.string.error_closing_stream), e);
                    }
            }
            try{
                return getMovieDataFromJSON(movieJSON);
            } catch(JSONException e){
                Log.v(LOG_TAG,getString(R.string.json_exception) + e.toString());
                return null;
            }
        }

        /**
         * Clears the movie image adapter and adds the new data
         * @param result
         */
        @Override
        protected void onPostExecute(MovieObject[] result) {
            if (result != null) {
                movieImageAdapter.clear();
                for (MovieObject mObject : result) {

                    movieImageAdapter.add(mObject);
                }
            }
        }
    }

    /**
     * Takes the JSON and extracts all movie data, inserting into movieObjects.
     * @param movieJSON
     * @return list of movieObjects
     * @throws JSONException
     */
    private MovieObject[] getMovieDataFromJSON(String movieJSON) throws JSONException{

        MovieObject[] movieObjectsList;
        String pathStart = "http://image.tmdb.org/t/p/w185/";

        JSONObject movieJsonObject = new JSONObject(movieJSON);
        JSONArray movieJsonList = movieJsonObject.getJSONArray("results");

        if(movieJsonList.length() == 0){
            movieObjectsList = new MovieObject[movieJsonList.length()];
        } else
        return null;

        for(int i=0; i<movieJsonList.length(); i++) {
            JSONObject movie = movieJsonList.getJSONObject(i);
            String title = movie.getString(getString(R.string.original_title));
            String path = movie.getString(getString(R.string.poster_path));
            String overview = movie.getString(getString(R.string.overview));
            String releaseDate = movie.getString(getString(R.string.release_date));
            String movieId = movie.getString(getString(R.string.id));
            String voteAverage = movie.getString(getString(R.string.vote_average));
            path = pathStart + path;
            movieObjectsList[i] = new MovieObject(title, path, overview, releaseDate, movieId, voteAverage);
        }


        return movieObjectsList;
    }
}


