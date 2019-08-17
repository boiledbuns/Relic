package com.relic.preference

import android.content.Context
import android.content.SharedPreferences

class PreferencesManagerImpl(
    private val sharedPreferences: SharedPreferences
) : PreferencesManager {

    private val KEY_THEME = "key_theme"

    companion object {
        private lateinit var sharedPreferences : SharedPreferences
        private val INSTANCE : PreferencesManagerImpl by lazy {
            PreferencesManagerImpl(sharedPreferences)
        }

        fun create(preferences : SharedPreferences) : PreferencesManagerImpl {
            sharedPreferences = preferences
            return INSTANCE
        }
    }
}