package com.relic.presentation.callbacks;

import com.relic.data.models.PostModel;

public interface PostLoadCallback {
  void onPostLoad(PostModel post);
}
