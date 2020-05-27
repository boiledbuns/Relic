package com.relic.domain.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UserModel : ListingItem() {
    var goldExpiration: String? = null
    var iconImg: String? = null
    var linkKarma = 0
    var commentKarma = 0
    var coins = 0
    var isFriend = false
    var isMod = false
}