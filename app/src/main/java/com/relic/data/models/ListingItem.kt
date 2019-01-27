package com.relic.data.models

private const val UNINITIALIZED = -1

open class ListingItem {
//    @Embedded(prefix = "src")
//    var postSource: PostSourceEntity? = null

    var userSubmittedPosition : Int? = UNINITIALIZED
    var userCommentsPosition : Int? = UNINITIALIZED
    var userSavedPosition : Int? = UNINITIALIZED
    var userUpvotedPosition : Int? = UNINITIALIZED
    var userDownvotedPosition : Int? = UNINITIALIZED
    var userGildedPosition : Int? = UNINITIALIZED
    var userHiddenPosition : Int? = UNINITIALIZED
}