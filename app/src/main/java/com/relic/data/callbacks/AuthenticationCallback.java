package com.relic.data.callbacks;

public interface AuthenticationCallback {
  /**
   * Callback to perform action once authentication has been confirmed
   */
  void onAuthenticated();
}
