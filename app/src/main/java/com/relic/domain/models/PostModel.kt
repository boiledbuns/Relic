package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class PostModel : ListingItem() {

    var title: String = ""

    var selftext: String? = null

    //  public String subName;
    //  public String stringDate;
    var score: Int = 0

    @Json(name = "num_comments")
    var commentCount: Int = 0
    var viewCount: Int = 0

    var domain: String? = null
    var url: String? = null
    var thumbnail: String? = null
        set(thumbnail) = if (thumbnail != null && thumbnail == "self") {
            field = null
        } else {
            field = thumbnail
        }

    @Json(name = "author_flair_text")
    var authorFlair: String? = null

    @Json(name = "link_flair_text")
    var linkFlair: String? = null

    @Json(name = "over_18")
    var nsfw: Boolean = false
    var stickied: Boolean = false
    var pinned: Boolean = false
    var locked: Boolean = false
    var archived: Boolean = false

    var platinum: Int = 0
    var gold: Int = 0
    var silver: Int = 0
    var self: Boolean = false

    companion object {
        var TYPE = "t3"
    }
}
