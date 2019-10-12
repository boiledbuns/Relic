package com.relic.preference

import android.app.Application
import android.preference.PreferenceManager

abstract class RelicPreferencesManager(
  app : Application
){
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(app)
}