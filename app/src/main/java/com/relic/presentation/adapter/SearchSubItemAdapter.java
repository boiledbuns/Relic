package com.relic.presentation.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.relic.R;

public class SearchSubItemAdapter extends CursorAdapter {

  public SearchSubItemAdapter(Context context, Cursor c, boolean autoRequery) {
    super(context, c, autoRequery);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    return LayoutInflater.from(context).inflate(R.layout.search_sub_item, viewGroup, false);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    // get values from cursor
    String subredditName = cursor.getString(cursor.getColumnIndexOrThrow("name"));

    TextView title = view.findViewById(R.id.searchsub_subname);
    title.setText(subredditName);
  }
}
