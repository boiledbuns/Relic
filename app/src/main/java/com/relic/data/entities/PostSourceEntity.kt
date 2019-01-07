package com.relic.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity
data class PostSourceEntity(
    @NonNull
    @PrimaryKey
    var sourceId : String,
    // all posts will have a subreddit regardless of its source
    var subreddit : String
) {
    // since a post can  only appear in any combination of the three sources
    var subredditPosition : Int = -1
    var frontpagePosition : Int  = -1
    var allPosition : Int = -1
}