/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.net.URI;

/**
 * Defines table and column names for the movie database.
 */
public class MovieContract {

    private static final String LOG_TAG = MovieContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.michaelcavalli.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movie";
    public static final String PATH_REVIEWS = "review";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_FAVORITES = "favorites";

    /*
        This class is the database to hold all of the individual movies and their associated data.
     */
    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Movie entry columns
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_SORT = "sort";
        public static final String COLUMN_ORDER = "list_order";

        // Build movie URI with selected movie ID attached
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Build movie URI for sort setting
        public static Uri buildMovieWithSort(String sort) {
            return CONTENT_URI.buildUpon().appendPath("SORT").appendPath(sort).build();
        }

        // Return the sort setting from the URI
        public static String getSortFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        // Return the selected movie ID from the URI
        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /*
        This class is the database to hold all of the reviews and their associated data.
     */
    public static final class ReviewEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // Table name
        public static final String TABLE_NAME = "review";

        //Review table columns
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_ORDER = "review_order";

        // Buld URI for reviews with selected movie ID attached
        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Build review URI with movie choice attached
        public static Uri buldReviewWithMovie(String movieChoice) {
            return CONTENT_URI.buildUpon().appendPath(movieChoice).build();
        }


    }

    /*
        This class is the database to hold all of the trailers and their associated data.
     */
    public static final class TrailerEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        // Table name
        public static final String TABLE_NAME = "trailer";

        // Columns for the trailer table
        public static final String COLUMN_NAME = "trailer_name";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_ORDER = "trailer_oder";
        public static final String COLUMN_KEY = "trailer_key";

        // Returns the URI with the movie ID attached
        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Returns the URI with the movie attached
        public static Uri buildTrailerWithMovie(String movieChoice) {
            return CONTENT_URI.buildUpon().appendPath(movieChoice).build();
        }
    }

    /*
        This class is the database to hold all of the favorite movies selected and their associated data.
     */
    public static final class FavoriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        // Table name
        public static final String TABLE_NAME = "favorite";

        // Favorite movie table columns
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        // Return URI with movie id attached
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Return the movie ID from the URI
        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
