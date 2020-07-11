package com.relic.domain.models

import androidx.room.Entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
open class PostModel : ListingItem() {

    var title: String = ""
    var selftext: String? = null

    @Json(name = "num_comments")
    var commentCount: Int = 0
//    @Json(name = "view_count")
//    var viewCount: Int = 0

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
    var spoiler : Boolean = false

    var subreddit : String? = null

    var platinum: Int = 0
    var gold: Int = 0
    var silver: Int = 0

    @Json(name = "is_self")
    var isSelf: Boolean = false
    @Json(name = "is_video")
    var isVideo : Boolean = false

    var ups : Int = 0
    var downs : Int = 0

//    var preview : String? = null
    var permalink : String? = null

//    @Json(name = "media_embed")
//    var embeddedMedia : String? = null

    var media : MediaList? = null

    companion object {
        var TYPE = "t3"
    }
}
