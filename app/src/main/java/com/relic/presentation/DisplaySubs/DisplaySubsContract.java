package com.relic.presentation.DisplaySubs;

import com.relic.data.SubRepository;

public interface DisplaySubsContract {
  interface VM {
    void init(SubRepository accountRepo);
  }

}
