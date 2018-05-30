package com.relic.presentation.frontpage;

import com.relic.data.Authenticator;
import com.relic.data.PostRepository;

public interface FrontpageContract {
  interface VM {
    void init(PostRepository postRepository, Authenticator auth);
  }

}
