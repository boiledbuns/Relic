package com.relic.presentation.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.relic.R;

public class RelicSearchView extends ViewGroup{
  RelicSearchView relicSearchView;
  boolean searchExpanded = false;

  public RelicSearchView(Context context) {
    super(context);
    relicSearchView = (RelicSearchView) inflate(context, R.layout.relic_searchview, this);

    // add onclick to show edit text
    findViewById(R.id.search_icon).setOnClickListener((View onClickView) -> {
      expandSearch(!searchExpanded);
      searchExpanded = !searchExpanded;
    });
  }

  public RelicSearchView(Context context, AttributeSet attributeSet) {
    this(context);
  }

  @Override
  protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

  }

  public void expandSearch (boolean expand) {
    int searchBox =  View.VISIBLE;
    int searchIcon = View.INVISIBLE;

    if (!expand) {
      searchBox = View.GONE;
      searchIcon = View.VISIBLE;
    }

    relicSearchView.findViewById(R.id.search_icon).setVisibility(searchIcon);
    relicSearchView.findViewById(R.id.search_box).setVisibility(searchBox);
  }


}
