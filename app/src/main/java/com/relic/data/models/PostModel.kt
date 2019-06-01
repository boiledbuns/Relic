package com.relic.data.models

import android.text.Html

import com.relic.domain.Post

open class PostModel : ListingItem(), Post {

    lateinit var title: String
    lateinit var created: String

    var selftext: String? = null
    var linkFlair: String? = null


    //  public String subName;
    //  public String stringDate;
    var score: Int = 0
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

    var authorFlair: String? = null

    var nsfw: Boolean = false
    var stickied: Boolean = false
    var pinned: Boolean = false
    var locked: Boolean = false
    var archived: Boolean = false

    var platinum: Int = 0
    var gold: Int = 0
    var silver: Int = 0
    var self: Boolean = false

    val htmlSelfText: String?
        get() = if (selftext == null) selftext else Html.fromHtml(Html.fromHtml(selftext).toString()).toString()

    companion object {
        var TYPE = "t3"
    }
}
