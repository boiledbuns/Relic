package com.relic.api.adapter

import com.relic.domain.models.CommentModel
import com.squareup.moshi.*

class CommentAdapter {
    private val options: JsonReader.Options = JsonReader.Options.of("kind", "data")

    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<CommentModel>) : CommentModel? {
        var comment: CommentModel? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                // we only want info nested inside the "data" object
                0 -> reader.skipValue()
                1 -> comment = delegate.fromJson(reader)
                else -> {
                    // skip everything else
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return comment
    }

    @Suppress("unused")
    @ToJson
    fun toJson(writer : JsonWriter, value : CommentModel) {}
}