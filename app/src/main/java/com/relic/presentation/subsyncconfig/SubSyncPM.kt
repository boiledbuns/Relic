package com.relic.presentation.subsyncconfig

import android.app.Application
import com.relic.preference.RelicPreferencesManager

private const val PREFIX_POST_SYNC = "POST_SYNC_"
private const val PREFIX_POST_SYNC_PAGES = "POST_SYNC_PAGES_"
private const val PREFIX_COMMENT_SYNC = "COMMENT_SYNC_"

class SubSyncPM (
    app : Application,
    postSourceName : String
) : RelicPreferencesManager(app){

    private val POST_SYNC_KEY = PREFIX_POST_SYNC + postSourceName
    private val POST_SYNC_PAGES = PREFIX_POST_SYNC_PAGES + postSourceName
    private val COMMENT_SYNC = PREFIX_COMMENT_SYNC + postSourceName

    var postSyncEnabled : Boolean
        get() = sharedPrefs.getBoolean(POST_SYNC_KEY, false)
        set(value) = sharedPrefs.edit().putBoolean(POST_SYNC_KEY, value).apply()

    var postSyncPages : Int
        get() = sharedPrefs.getInt(POST_SYNC_PAGES, 0)
        set(value) = sharedPrefs.edit().putInt(POST_SYNC_PAGES, value).apply()

    var commentSyncEnabled : Boolean
        get() = sharedPrefs.getBoolean(COMMENT_SYNC, false)
        set(value) = sharedPrefs.edit().putBoolean(COMMENT_SYNC, value).apply()
}