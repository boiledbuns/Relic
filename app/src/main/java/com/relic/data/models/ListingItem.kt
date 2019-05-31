package com.relic.data.models

open class ListingItem {

    // the id is the "full name" of an item
    open var fullName = ""
    var visited: Boolean = false
    var userUpvoted: Int = 0
    var saved: Boolean = false
    var subreddit : String? = null

    var userSubmittedPosition: Int = 0
    var userCommentsPosition: Int = 0
    var userSavedPosition: Int = 0
    var userUpvotedPosition: Int = 0
    var userDownvotedPosition: Int = 0
    var userGildedPosition: Int = 0
    var userHiddenPosition: Int = 0
}