package com.relic.scheduler

import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType

interface ScheduleManager {
    fun setupPostSync(
      postSource: PostSource,
      pagesToSync : Int,
      timeToSync : Int,
      repeatType : SyncRepeatOption,
      repeatDay : SyncRepeatDays,
      sortType: SortType,
      sortScope: SortScope,
      commentSyncEnabled : Boolean = false
    )

    fun cancelPostSync(postSource: PostSource)
}

enum class SyncRepeatOption {
    DAILY, WEEKLY
}

enum class SyncRepeatDays {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}