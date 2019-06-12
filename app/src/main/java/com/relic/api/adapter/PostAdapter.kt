package com.relic.api.adapter

import com.relic.domain.models.PostModel
import com.squareup.moshi.*

/**
 * This adapter is used for unwrapping the nested data from a "post" object in a json response
 */
class PostAdapter {
    private val options: JsonReader.Options = JsonReader.Options.of("kind", "data")

    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<PostModel>) : PostModel? {
        var post: PostModel? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                // we only want info nested inside the "data" object
                0 -> reader.skipValue()
                1 -> post = delegate.fromJson(reader)
                else -> {
                    // skip everything else
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return post
    }

    @Suppress("unused")
    @ToJson
    fun toJson(writer : JsonWriter, value : PostModel) {}
}