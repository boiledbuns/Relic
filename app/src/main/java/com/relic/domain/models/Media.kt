package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaList (
    @Json(name = "reddit_video")
    val video : RedditVideo
)

interface Media

@JsonClass(generateAdapter = true)
data class RedditVideo(
    val fallback_url : String?,
    val height : String?,
    val width : String?,
    val scrubber_media_url : String?,
    val dash_url : String?,
    val duration : Int,
    val hls_url : String?,
    val is_gif : Boolean,
    val transcoding_status: String
) : Media