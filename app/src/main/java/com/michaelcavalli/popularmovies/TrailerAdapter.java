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
import android.widget.TextView;


/**
 * Created by silen_000 on 11/1/2015.
 */
public class TrailerAdapter extends CursorAdapter {

    Context mContext;
    private final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    /**
     * Constructor
     * @param context
     * @param c
     * @param flags
     */
    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    /**
     * Sets the text for each trailer in the list
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.name.setText(cursor.getString(ReviewFragment.COL_AUTHOR));

    }

    // Returns the view for each trailer
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_trailer, parent, false);

        ViewHolder reviewHolder = new ViewHolder(view);

        view.setTag(reviewHolder);

        return view;
    }

    // Viewholder for each list item
    public static class ViewHolder {
        public final TextView name;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.trailer_name);
        }
    }

}