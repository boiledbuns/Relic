package com.relic.api.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Listing <T> (
    val kind : String,
    val data : Data<T>
)

