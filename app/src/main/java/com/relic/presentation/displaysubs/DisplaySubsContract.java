package com.relic.presentation.displaysubs;

import com.relic.data.SubRepository;
import com.relic.domain.Subreddit;

import java.util.List;

public interface DisplaySubsContract {

  interface VM {
    void init(SubRepository accountRepo);
  }

}
