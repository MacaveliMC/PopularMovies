/**
 * Copyright (C) 2015 Michael Cavalli
 */

package com.michaelcavalli.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by silen_000 on 10/29/2015.
 */
public class MovieAdapter extends CursorAdapter {

    Context mContext;
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    /**
     * Construtor
     * @param context
     * @param c
     * @param flags
     */
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    /**
     * Set the poster image for each grid spot
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView mView = (ImageView) view;
        //ViewHolder viewHolder = (ViewHolder) mView.getTag();


        String posterPath = cursor.getString(MainActivityFragment.COL_POSTER_PATH);

        Picasso.with(mContext).load(posterPath).into(mView);

    }

    /**
     * Creates a new view to be filled with content
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView mView = (ImageView) LayoutInflater.from(context).inflate(R.layout.movie_grid, parent, false);
        mView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mView.setAdjustViewBounds(true);
        return mView;
    }
}
