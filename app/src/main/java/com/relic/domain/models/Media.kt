package com.relic.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaList(
    @Json(name = "reddit_video")
    val video: RedditVideo?,
    val oembed: Oembed?
)

interface Media {

}

@JsonClass(generateAdapter = true)
data class RedditVideo(
    val fallback_url: String?,
    val height: Int,
    val width: Int,
    val scrubber_media_url: String?,
    val dash_url: String?,
    val duration: Int,
    val hls_url: String?,
    val is_gif: Boolean,
    val transcoding_status: String
) : Media

@JsonClass(generateAdapter = true)
data class Oembed(
    val provider_url: String,
    val description: String?,
    val title: String?,
    val author_name: String?,
    val height: Int,
    val width: Int,
    val html: String?,
    val thumbnail_width: Int?,
    val thumbnail_height: Int?,
    val version: String?,
    val provider_name: String,
    val thumbnail_url: String?,
    val type: String?
) : Media