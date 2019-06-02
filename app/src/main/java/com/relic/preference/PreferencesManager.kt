package com.relic.preference

interface PreferencesManager {
    fun getApplicationTheme() : Int
    fun setApplicationTheme(themeResId : Int)
}