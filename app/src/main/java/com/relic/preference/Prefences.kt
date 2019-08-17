package com.relic.preference

/**
 * defines preference interfaces for each screen that may need it
**/

interface SubViewPreferences {

}

// posts being displayed together (ie. within a subreddit or profile page)
interface PostViewPreferences {
    fun setPostCardStyle(cardStyle : Int)
    fun getPostCardStyle() : Int
}

// single, full post being displayed
interface FullPostViewPreferences {

}

const val POST_LAYOUT_SPAN = 0
const val POST_LAYOUT_CARD = 1


