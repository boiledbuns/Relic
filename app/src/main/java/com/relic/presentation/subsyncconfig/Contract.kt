package com.relic.presentation.subsyncconfig

import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.scheduler.SyncRepeatDays
import com.relic.scheduler.SyncRepeatOption

interface SubSyncPreferenceManager {
    fun isPostSyncEnabled() : Boolean
    // time to sync posts
    fun postSyncTime() : Int
    // how often to repeat sync (ie. daily or weekly)
    fun postSyncRepeat() : SyncRepeatOption
    // if weekly, what day to sync
    fun postSyncRepeatDays() : SyncRepeatDays?
    fun postSyncRepeatPages() : Int

    fun postSortType() : SortType
    fun postSortScope() : SortScope

    fun isCommentSyncEnabled() : Boolean
}