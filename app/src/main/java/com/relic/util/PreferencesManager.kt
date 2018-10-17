package com.relic.util

class PreferencesManager {

    companion object {
        private val INSTANCE : PreferencesManager by lazy {
            PreferencesManager()
        }

        fun get() : PreferencesManager {
            return INSTANCE
        }
    }

}