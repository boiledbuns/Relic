package com.relic.presentation.util

import com.relic.domain.models.PostModel

/**
 * currently supported media types:
 * -> v.redd.it
 * -> gfycat
 *
 */
sealed class MediaType {
    object Image : MediaType()
    object Link : MediaType()
    data class Gfycat(
        val mediaUrl: String
    ) : MediaType()

    data class VReddit(
        val mediaUrl: String,
        val audioUrl: String
    ) : MediaType()
}

object MediaHelper {

    private val validImageEndings = listOf("jpg", "png", "gif")

    // currently supported
    fun determineType(postModel: PostModel): MediaType? {
        var type: MediaType? = null
        val url = postModel.url

        // check url ending to see if it's an image
        if (url != null) {
            val media = postModel.media
            if (media != null) {
                if (media.video != null) {
                    // reddit separates video and audio files
                    // to access the audio mp4, change ...DASH_720.mp4 to DASH_audio.mp4
                    val mediaUrl = postModel.media!!.video!!.fallback_url!!
                    val audioUrl = mediaUrl.replace("[0-9]+.mp4".toRegex(), "audio.mp4")
                    type = MediaType.VReddit(mediaUrl = mediaUrl, audioUrl = audioUrl)
                } else if (media.oembed != null && media.oembed.provider_name == "Gfycat") {
                    type = MediaType.Gfycat(url)
                }
            } else {
                val lastThree = url.substring(url.length - 3)
                if (validImageEndings.contains(lastThree)) {
                    type = MediaType.Image
                } else if (!postModel.isSelf) {
                    type = MediaType.Link
                }
            }
        }

        return type
    }
}