package com.relic.data.gateway;

import android.arch.lifecycle.LiveData;

public interface SubGateway {
  LiveData<String> getAdditionalSubInfo(String subredditName);
}
