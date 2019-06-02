package com.relic.presentation.adapter;

import com.relic.domain.models.SubredditModel;

public interface SubItemOnClick {
  void onClick(SubredditModel subItem);
  boolean onLongClick(SubredditModel subItem);
}
