package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CommentModel: ListingItem() {

    var id: String? = null

    @Json(name = "body_html")
    var body: String = ""
    var created: String? = null
    var score: Int = 0

    // fullname of this comment's direct ancestor (could be a post or comment)
    @Json(name = "parent_id")
    var parentFullname: String = ""

    // fullname of this comment's root post
    @Json(name = "link_id")
    var linkFullname: String? = null

    var authorFlairText: String? = null

    var platinum: Int = 0
    var gold: Int = 0
    var silver: Int = 0

    var isSubmitter: Boolean = false

//    var edited: String? = null
    var depth: Int = 0
    var replyCount: Int = 0
    var replyLink: String? = null

    @Json(name = "link_title")
    var linkTitle: String? = null

    @Json(name = "link_author")
    var linkAuthor: String? = null

    var position: Float = 0.toFloat()

    val isLoadMore: Boolean
        get() = author == ""

    companion object {
        var UPVOTE = 1
        var DOWNVOTE = -1
        var NOVOTE = 0
        var TYPE = "t1_"
    }
}
