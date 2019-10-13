package com.relic.scheduler

import com.relic.data.PostSource

interface ScheduleManager {
    fun setupPostSync(postSource: PostSource)
    fun cancelPostSync(postSource: PostSource)
}