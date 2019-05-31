package com.relic.util

import com.relic.data.models.PostModel

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
            if (postModel.domain == "gfycat.com") {
                type = MediaType.Gfycat
            } else if (postModel.domain == "v.redd.it") {
                type = MediaType.VReddit
            }
            else {
                val url = postModel.url!!
                val lastThree = url.substring(url.length - 3)
                if (validImageEndings.contains(lastThree)) {
                    type = MediaType.Image
                } else if (!postModel.self) {
                    type = MediaType.Link
                }
            }
        }

        return type
    }
}