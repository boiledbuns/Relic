package com.relic.presentation.adapter;

import com.relic.data.models.SubredditModel;

public interface SubItemOnClick {
  void onClick(SubredditModel subItem);
  void onLongClick(SubredditModel subItem);
}
