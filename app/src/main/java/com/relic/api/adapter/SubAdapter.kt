package com.relic.api.adapter

import com.relic.domain.models.SubredditModel
import com.squareup.moshi.*

class SubAdapter {
    private val options: JsonReader.Options = JsonReader.Options.of("kind", "data")

    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<SubredditModel>) : SubredditModel? {
        var sub: SubredditModel? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                // we only want info nested inside the "data" object
                0 -> reader.skipValue()
                1 -> sub = delegate.fromJson(reader)
                else -> {
                    // skip everything else
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return sub
    }

    @Suppress("unused")
    @ToJson
    fun toJson(writer : JsonWriter, value : SubredditModel) {}
}