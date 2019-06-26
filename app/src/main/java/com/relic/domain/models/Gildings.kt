package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Gildings {
    @Json(name = "gid_1")
    var platinum : Int = 0

    @Json(name = "gid_2")
    var gold : Int = 0

    @Json(name = "gid_3")
    var silver : Int = 0
}