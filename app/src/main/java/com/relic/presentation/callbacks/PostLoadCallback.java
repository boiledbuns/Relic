package com.relic.presentation.callbacks;

import com.relic.domain.models.PostModel;

public interface PostLoadCallback {
  void onPostLoad(PostModel post);
}
