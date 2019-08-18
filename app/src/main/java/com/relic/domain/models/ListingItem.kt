package com.relic.domain.models

import android.arch.persistence.room.PrimaryKey
import com.relic.api.qualifier.Date
import com.relic.api.qualifier.Likes
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class ListingItem {

    @PrimaryKey
    var id: String = ""

    // the id is the "full name" of an item
    @Json(name = "name")
    var fullName = ""
    var author: String = ""
    var score: Int = 0

    @Date
    @Json(name = "created_utc")
    var created: java.util.Date? = null

    var visited: Boolean = false

    @Json(name = "likes")
    @Likes var userUpvoted: Int = 0

    var gildings : Gildings? = null

    var saved: Boolean = false
    open var subreddit : String? = null

    var userSubmittedPosition: Int = 0
    var userCommentsPosition: Int = 0
    var userSavedPosition: Int = 0
    var userUpvotedPosition: Int = 0
    var userDownvotedPosition: Int = 0
    var userGildedPosition: Int = 0
    var userHiddenPosition: Int = 0
}