package com.relic.util

interface PreferencesManager {
    fun getApplicationTheme() : Int?
    fun setApplicationTheme(themeResId : Int)
}