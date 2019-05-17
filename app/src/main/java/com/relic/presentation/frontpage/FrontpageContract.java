package com.relic.presentation.frontpage;

import com.relic.data.auth.AuthenticatorImpl;
import com.relic.data.PostRepository;

public interface FrontpageContract {
  interface VM {
    void init(PostRepository postRepository, AuthenticatorImpl auth);
  }

}
