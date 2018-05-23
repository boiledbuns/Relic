package com.relic.presentation.Frontpage;

import com.relic.data.PostRepository;

public interface FrontpageContract {
  interface VM {
    void init(PostRepository postRepository);
  }

}
