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
public class ReviewAdapter extends CursorAdapter {

    Context mContext;
    private final String LOG_TAG = ReviewAdapter.class.getSimpleName();

    /**
     * Constructor
     * @param context
     * @param c
     * @param flags
     */
    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    /**
     * Adds the review info to the view elements
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.author.setText(cursor.getString(ReviewFragment.COL_AUTHOR));
        viewHolder.content.setText(cursor.getString(ReviewFragment.COL_CONTENT));

    }

    /**
     * Creates a new view to be used for the reviews
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);

        ViewHolder reviewHolder = new ViewHolder(view);

        view.setTag(reviewHolder);

        return view;
    }

    /**
     * A viewholder to hold references to the elements being used in the layout
     */
    public static class ViewHolder {
        public final TextView author;
        public final TextView content;

        public ViewHolder(View view) {
            author = (TextView) view.findViewById(R.id.review_author);
            content = (TextView) view.findViewById(R.id.review_text);
        }
    }

}