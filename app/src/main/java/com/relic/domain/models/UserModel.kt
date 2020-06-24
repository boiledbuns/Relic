package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UserModel : ListingItem() {
    var username: String
        get() = fullName
        set(newUsername) {
            fullName = newUsername
        }

    // TODO: temp, convert to date after updating adapter
    var createdDate: String = ""

    var goldExpiration: String? = null
    @Json(name = "icon_img")
    var iconImg: String? = null
    @Json(name = "link_karma")
    var linkKarma = 0
    @Json(name = "comment_karma")
    var commentKarma = 0
    var coins = 0
    var isFriend = false
    var isMod = false
}