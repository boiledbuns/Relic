package com.relic.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

private const val UNINITIALIZED = -1
@Entity
data class PostSourceEntity(
    @NonNull
    @PrimaryKey
    var sourceId : String,
    // all posts will have a subreddit regardless of its source
    var subreddit : String
) {
    var commentId : String? = null

    // since a post can  only appear in any combination of the three sources
    var subredditPosition : Int = UNINITIALIZED
    var frontpagePosition : Int  = UNINITIALIZED
    var allPosition : Int = UNINITIALIZED

//    var userSubmissionPosition : Int = UNINITIALIZED

    /**
     * Just a note, it's not enough for us to just use user submissions for displaying
     * content related to the user. If we used sorting to display values, the very likely
     * chance of overlap between items in two categories (eg. upvoted and saved) means
     * that if comments are fetched for the first type (upvoted), then results that also
     * overlap with the second type (saved) will show up when displaying the second type
     *
     * This can be misleading and mislead users to believe there are fewer results from
     * the second type (saved) than there are. As a result, we want to be verbose about
     * where the items are being shown
     */
    var userSubmittedPosition : Int = UNINITIALIZED
    var userCommentsPosition : Int = UNINITIALIZED
    var userSavedPosition : Int = UNINITIALIZED
    var userUpvotedPosition : Int = UNINITIALIZED
    var userDownvotedPosition : Int = UNINITIALIZED
    var userGildedPosition : Int = UNINITIALIZED
    var userHiddenPosition : Int = UNINITIALIZED
}