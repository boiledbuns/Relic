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
    object Gfycat : MediaType()
    object VReddit : MediaType()
}

object MediaHelper {

    private val validImageEndings = listOf("jpg", "png", "gif")

    // currently supported
    fun determineType(postModel: PostModel): MediaType? {
        var type : MediaType? = null

        // check url ending to see if it's an image
        if (postModel.url != null) {
            val media = postModel.media
            if (media != null) {
                if (media.video != null) {
                    type = MediaType.VReddit
                }
                else if (media.oembed != null && media.oembed.type == "gfycat.com") {
                    type = MediaType.Gfycat
                }
            }
            else {
                val url = postModel.url!!
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