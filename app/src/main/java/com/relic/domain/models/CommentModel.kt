package com.relic.domain.models

import com.relic.api.response.Listing
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CommentModel : ListingItem() {

    var id: String? = null
    var body: String = ""
    var created: String? = null
    var score: Int = 0

    // post parent fullname, NOT id
    var parentPostId: String = ""

    // fullname of the parent post
    @Json(name = "link_id") var parentPost: String? = null

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

    override var fullName: String
        get() = if (id == null) "" else "t1_$id"
        set(value: String) {
            super.fullName = value
        }

//    var replies: Listing<CommentModel>? = null

    companion object {
        var UPVOTE = 1
        var DOWNVOTE = -1
        var NOVOTE = 0
        var TYPE = "t1_"
    }
}
