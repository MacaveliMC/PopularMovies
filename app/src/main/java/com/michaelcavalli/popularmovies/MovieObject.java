/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.graphics.Movie;

/**
 * Movie object to hold all the data for each movie
 */
public class MovieObject {
    private String movieTitle;
    private String moviePosterPath;
    private String movieOverview;
    private String movieReleaseDate;
    private String movieId;
    private String movieVoteAverage;

    /**
     * Constructor for the movieObject.  Sets all the relevant data immediately.
     * @param title title of the movie
     * @param path path to the movie poster
     * @param overview overview of the movie
     * @param releaseDate date the movie was released
     * @param id id of the movie for future direct url to the movie info
     * @param voteAverage average rating of the movie
     */
    public MovieObject(String title, String path, String overview, String releaseDate, String id, String voteAverage) {
        movieTitle = title;
        moviePosterPath = path;
        movieOverview = overview;
        movieReleaseDate = releaseDate;
        movieId = id;
        movieVoteAverage = voteAverage;
    }

    /**
     * sets the movie title
     * @param title
     * @return
     */
    public boolean setMovieTitle(String title) {
        if (title != null) {
            movieTitle = title;
            return true;
        } else
            return false;
    }

    /**
     * sets the poster path
     * @param path
     * @return
     */
    public boolean setMoviePosterPath(String path) {
        if (path != null) {
            moviePosterPath = path;
            return true;
        } else
            return false;
    }

    /**
     * sets the movie overview
     * @param overview
     * @return
     */
    public boolean setMovieOverview(String overview) {
        if (overview != null) {
            movieOverview = overview;
            return true;
        } else
            return false;
    }

    /**
     * sets the release date
     * @param releaseDate
     * @return
     */
    public boolean setMovieReleaseDate(String releaseDate) {
        if (releaseDate != null) {
            movieReleaseDate = releaseDate;
            return true;
        } else
            return false;
    }

    /**
     * sets the movie id
     * @param id
     * @return
     */
    public boolean setMovieId(String id) {
        if (id != null) {
            movieId = id;
            return true;
        } else
            return false;
    }

    /**
     * sets the vote average
     * @param voteAverage
     * @return
     */
    public boolean setVoteAverage(String voteAverage) {
        if (voteAverage != null) {
            movieVoteAverage = voteAverage;
            return true;
        } else
            return false;
    }

    /**
     * returns the movie title
     * @return
     */
    public String getMovieTitle() {
        if (movieTitle != null) {
            return movieTitle;
        } else
            return null;
    }

    /**
     * returns the poster path
     * @return
     */
    public String getMoviePosterPath() {
        if (moviePosterPath != null) {
            return moviePosterPath;
        } else
            return null;
    }

    /**
     * returns the movie overview
     * @return
     */
    public String getMovieOverview() {
        if (movieOverview != null) {
            return movieOverview;
        } else
            return null;
    }

    /**
     * returns the release date
     * @return
     */
    public String getMovieReleaseDate() {
        if (movieReleaseDate != null) {
            return movieReleaseDate;
        } else
            return null;
    }

    /**
     * returns the movie id
     * @return
     */
    public String getMovieId() {
        if (movieId != null) {
            return movieId;
        } else
            return null;
    }

    /**
     * returns the vote average
     * @return
     */
    public String getMovieVoteAverage() {
        if (movieVoteAverage != null) {
            return movieVoteAverage;
        } else
            return null;
    }

}
