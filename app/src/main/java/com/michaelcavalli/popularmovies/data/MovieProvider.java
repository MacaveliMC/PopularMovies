/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Movie;
import android.net.Uri;
import android.util.Log;


/**
 * This is the content provider that communicates between the app, adapters, and the data in the
 * SQLite databases.
 */
public class MovieProvider extends ContentProvider {

    private final String LOG_TAG = MovieProvider.class.getSimpleName();

    private MovieDbHelper mMovieDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int MOVIE = 100;
    static final int MOVIE_WITH_SORT = 101;
    static final int MOVIE_SELECTION = 102;
    static final int REVIEW = 200;
    static final int REVIEW_WITH_MOVIE = 201;
    static final int TRAILER = 300;
    static final int TRAILER_WITH_MOVIE = 301;
    static final int FAVORITE = 400;
    static final int FAVORITE_WITH_MOVIE = 401;

    private static final SQLiteQueryBuilder sMovieQuery;
    private static final SQLiteQueryBuilder sReviewQuery;
    private static final SQLiteQueryBuilder sTrailerQuery;
    private static final SQLiteQueryBuilder sFavoriteQuery;

    // Constructor that sets the tables to be searched
    static {
        sMovieQuery = new SQLiteQueryBuilder();
        sReviewQuery = new SQLiteQueryBuilder();
        sTrailerQuery = new SQLiteQueryBuilder();
        sFavoriteQuery = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //ReviewEntry INNER JOIN MovieEntry ON MovieEntry.movie_title = ReviewEntry.movie_title
        sMovieQuery.setTables(MovieContract.MovieEntry.TABLE_NAME);
        sFavoriteQuery.setTables(MovieContract.FavoriteEntry.TABLE_NAME);
        sReviewQuery.setTables(
                MovieContract.ReviewEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.ReviewEntry.TABLE_NAME +
                        "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
        sTrailerQuery.setTables(
                MovieContract.TrailerEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.TrailerEntry.TABLE_NAME +
                        "." + MovieContract.TrailerEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
    }

    private static final String sMovieWithTitleSelection = MovieContract.MovieEntry.TABLE_NAME +
            "." + MovieContract.MovieEntry._ID + " = ? ";

    private static final String sMovieWithSortSelection = MovieContract.MovieEntry.TABLE_NAME +
            "." + MovieContract.MovieEntry.COLUMN_SORT + " = ? ";

    private static final String sReviewWithTitleSelection = MovieContract.ReviewEntry.TABLE_NAME +
            "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sTrailerWithTitleSelection = MovieContract.TrailerEntry.TABLE_NAME +
            "." + MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sFavoriteWithTitleSelection = MovieContract.FavoriteEntry.TABLE_NAME +
            "." + MovieContract.FavoriteEntry._ID + " = ? ";

    private Cursor getMovieWithId(Uri uri, String[] projection, String sortOrder) {
        String id = MovieContract.MovieEntry.getIdFromUri(uri);

        String selectionArgs[] = new String[]{id};
        String selection = sMovieWithTitleSelection;

        return sMovieQuery.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getMovieWithSort(Uri uri, String[] projection, String sortOrder) {
        String title = MovieContract.MovieEntry.getSortFromUri(uri);

        String selectionArgs[] = new String[]{title};
        String selection = sMovieWithSortSelection;

        return sMovieQuery.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getReviewWithId(Uri uri, String[] projection, String sortOrder) {
        String id = MovieContract.MovieEntry.getIdFromUri(uri);

        String selectionArgs[] = new String[]{id};
        String selection = sReviewWithTitleSelection;

        return sReviewQuery.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getTrailerWithId(Uri uri, String[] projection, String sortOrder){
        String id = MovieContract.MovieEntry.getIdFromUri(uri);

        String selectionArgs[] = new String[]{id};
        String selection = sTrailerWithTitleSelection;

        return sTrailerQuery.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getFavoriteWithId(Uri uri, String[] projection, String sortOrder) {
        String id = MovieContract.FavoriteEntry.getIdFromUri(uri);

        String selectionArgs[] = new String[]{id};
        String selection = sFavoriteWithTitleSelection;

        return sFavoriteQuery.query(mMovieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/SORT/*", MOVIE_WITH_SORT);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/*", MOVIE_SELECTION);
        matcher.addURI(authority, MovieContract.PATH_REVIEWS, REVIEW);
        matcher.addURI(authority, MovieContract.PATH_REVIEWS + "/*", REVIEW_WITH_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_TRAILERS, TRAILER);
        matcher.addURI(authority, MovieContract.PATH_TRAILERS + "/*", TRAILER_WITH_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_FAVORITES, FAVORITE);
        matcher.addURI(authority, MovieContract.PATH_FAVORITES + "/*", FAVORITE_WITH_MOVIE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            // For all movies
            case MOVIE: {
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // For retrieving movies with the sort selection
            case MOVIE_WITH_SORT: {
                retCursor = getMovieWithSort(uri, projection, sortOrder);
                break;
            }
            // For selecting a movie for the detail view
            case MOVIE_SELECTION: {
                retCursor = getMovieWithId(uri, projection, sortOrder);
                break;
            }
            // For all reviews
            case REVIEW: {
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // For retrieving reviews for a specific movie
            case REVIEW_WITH_MOVIE: {
                retCursor = getReviewWithId(uri, projection, sortOrder);
                break;
            }
            // For all trailers
            case TRAILER: {
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // For retrieving trailers for a selected movie
            case TRAILER_WITH_MOVIE: {
                retCursor = getTrailerWithId(uri, projection, sortOrder);
                break;
            }
            // For all favorite movies
            case FAVORITE: {
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // For selecting a favorite movie for the detail view
            case FAVORITE_WITH_MOVIE: {
                retCursor = getFavoriteWithId(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            // Inserting movies
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert new row into " + uri);
                break;
            }
            // Inserting reviews
            case REVIEW: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert new row into " + uri);
                break;
            }
            // Inserting trailers
            case TRAILER: {
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if(_id > 0)
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert new row into " + uri);
                break;
            }
            // Inserting favorites
            case FAVORITE: {
                long _id = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.FavoriteEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert new row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;

        switch (match) {
            // Inserting movies
            case MOVIE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            // Inserting reviews
            case REVIEW:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                    ;
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            // Inserting Trailers
            case TRAILER:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                    ;
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            // Inserting favorite movies
            case FAVORITE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILER:
                rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case FAVORITE:
                rowsUpdated = db.update(MovieContract.FavoriteEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            // Deleting movies
            case MOVIE:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // Deleting reviews
            case REVIEW:
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // Deleting trailers
            case TRAILER:
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // Deleting favorites
            case FAVORITE:
                rowsDeleted = db.delete(MovieContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);


        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_SELECTION:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_SORT:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_WITH_MOVIE:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_WITH_MOVIE:
                return  MovieContract.TrailerEntry.CONTENT_TYPE;
            case FAVORITE:
                return MovieContract.FavoriteEntry.CONTENT_TYPE;
            case FAVORITE_WITH_MOVIE:
                return MovieContract.FavoriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
