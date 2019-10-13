package com.relic.scheduler

import com.relic.data.PostSource

interface ScheduleManager {
    fun setupPostSync(postSource: PostSource, syncPages : Int, time : Long)
    fun cancelPostSync(postSource: PostSource)
}