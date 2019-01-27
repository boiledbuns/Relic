package com.relic.data.models

import android.arch.persistence.room.Ignore

private const val UNINITIALIZED = -1

open class ListingItem {
    var id: String = ""
    @Ignore
    var isVisited: Boolean = false

    var userSubmittedPosition : Int? = UNINITIALIZED
    var userCommentsPosition : Int? = UNINITIALIZED
    var userSavedPosition : Int? = UNINITIALIZED
    var userUpvotedPosition : Int? = UNINITIALIZED
    var userDownvotedPosition : Int? = UNINITIALIZED
    var userGildedPosition : Int? = UNINITIALIZED
    var userHiddenPosition : Int? = UNINITIALIZED
}