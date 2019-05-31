package com.relic.data.deserializer

import com.relic.data.models.PostModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Response (
    @field:Json(name = "data") val data : Data
)

data class Data (
    @field:Json(name = "children") val data : List<PostModel>,
    @field:Json(name = "after") val after : String
)

@JsonClass(generateAdapter = true)
data class JPostModel(
    val test : String = ""
) : PostModel()