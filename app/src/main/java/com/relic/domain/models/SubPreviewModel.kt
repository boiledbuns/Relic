package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubPreviewModel(
    @Json(name = "icon_img") val icon : String?,
    @Json(name = "key_color") val keyColor : String,
    @Json(name = "active_user_count") val activeUsers : Long,
    @Json(name = "subscriber_count") val subs : Long,
    @Json(name = "allow_images") val imagesAllowed : Boolean,
    @Json(name = "name") val name : String
)