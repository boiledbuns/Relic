package com.relic.presentation.subsyncconfig

interface SubSyncPreferenceManager {
    fun isPostSyncEnabled() : Boolean
    // time to sync posts
    fun postSyncTime() : String?
    // how often to repeat sync (ie. daily or weekly)
    fun postSyncRepeat() : String?
    // if weekly, what day to sync
    fun postSyncRepeatDays() : String?
    fun postSyncRepeatPages() : Int

    fun isCommentSyncEnabled() : Boolean
}