package com.relic.presentation.frontpage;

import com.relic.data.auth.AuthImpl;
import com.relic.data.PostRepository;

public interface FrontpageContract {
  interface VM {
    void init(PostRepository postRepository, AuthImpl auth);
  }

}
