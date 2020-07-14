package com.relic.domain.models

import androidx.room.Entity
import com.relic.api.qualifier.ConvertHtml
import com.relic.api.qualifier.Date
import com.relic.api.qualifier.More
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
open class CommentModel: ListingItem() {

    @ConvertHtml
    @Json(name = "body_html")
    var bodyHtml: String = ""

    @ConvertHtml
    var body: String = ""

    // id of this comment's direct ancestor (could be a post or comment)
    // this is a fullname (ignore the fact the field says id)
    @Json(name = "parent_id")
    var parentFullname: String = ""

    // fullname of this comment's root post
    // this is a fullname (ignore the fact the field says id)
    @Json(name = "link_id")
    var linkFullname: String? = null

    var authorFlairText: String? = null

    var platinum: Int = 0
    var gold: Int = 0
    var silver: Int = 0

    @Json(name = "is_submitter")
    var isSubmitter: Boolean = false

    var subreddit : String? = null

    @Date
    var edited: java.util.Date? = null

    var depth: Int = 0
    var replyCount: Int = 0
    var replyLink: String? = null

    @Json(name = "link_title")
    var linkTitle: String? = null

    @Json(name = "link_author")
    var linkAuthor: String? = null

    var position: Float = 0F

    val isLoadMore: Boolean
        get() = author == ""

    // use only if this is a "more" comment in which case this will be a list of ids
    @More
    @Json(name = "children")
    var more : List<String>? = null

    companion object {
        var UPVOTE = 1
        var DOWNVOTE = -1
        var NOVOTE = 0
        var TYPE = "t1_"
    }
}
